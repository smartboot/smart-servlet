/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.session;

import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.waf.WafException;
import org.smartboot.socket.timer.HashedWheelTimer;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.provider.SessionProvider;

import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
class SessionProviderImpl implements SessionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionProviderImpl.class);
    private final static byte[] DEFAULT_BYTES = ("smart-servlet:" + Container.VERSION).getBytes();
    private final static int maskLength = 4;
    private static final String MAGIC_NUMBER = "ss";

    /**
     * 默认超时时间：30分钟
     */
    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;
    /**
     * 当前会话集合
     */
    private final Map<String, HttpSessionImpl> sessionMap = new ConcurrentHashMap<>();
    /**
     * 会话超时时间
     */
    private int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;

    private final HashedWheelTimer timer = new HashedWheelTimer(r -> new Thread(r, "smartboot-session-timer"), 10, 64);


    @Override
    public HttpSessionImpl getSession(HttpServletRequestImpl request, HttpServletResponse response, boolean create) {
        HttpSessionImpl httpSession = getSession(request);
        if (create && httpSession == null) {
            /**
             * javax.servlet.http.HttpServletRequest#getSession(boolean)接口规范：
             * If the container is using cookies to maintain session integrity and is asked to create a new session when the response is committed, an IllegalStateException is thrown.
             */
            if (response.isCommitted()) {
                throw new IllegalStateException("response has already committed!");
            }
            httpSession = new HttpSessionImpl(this, createSessionId(), request.getServletContext()) {
                @Override
                public void invalid() {
                    try {
                        super.invalid();
                    } finally {
                        sessionMap.remove(getId());
                    }
                }
            };
            httpSession.setMaxInactiveInterval(maxInactiveInterval);
            SessionCookieConfig sessionCookieConfig = request.getServletContext().getSessionCookieConfig();
            Cookie cookie = new Cookie(sessionCookieConfig.getName(), httpSession.getId());
            cookie.setPath(sessionCookieConfig.getPath());
            if (sessionCookieConfig.getDomain() != null) {
                cookie.setDomain(sessionCookieConfig.getDomain());
            }
            cookie.setHttpOnly(sessionCookieConfig.isHttpOnly());
            cookie.setSecure(sessionCookieConfig.isSecure());
            cookie.setMaxAge(sessionCookieConfig.getMaxAge());
            response.addCookie(cookie);
            sessionMap.put(httpSession.getId(), httpSession);
            request.setActualSessionId(httpSession.getId());
        }
        if (httpSession != null) {
            httpSession.setResponse(response);
        }
        return httpSession;
    }

    @Override
    public void changeSessionId(HttpSession httpSession) {
        if (!(httpSession instanceof HttpSessionImpl)) {
            throw new IllegalStateException();
        }
        HttpSessionImpl session = sessionMap.remove(httpSession.getId());
        session.changeSessionId(String.valueOf(System.currentTimeMillis()));
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void updateAccessTime(HttpServletRequestImpl request) {
        HttpSessionImpl session = getSession(request);
        if (session != null) {
            session.setLastAccessed(System.currentTimeMillis());
        }
    }

    @Override
    public void pauseAccessTime(HttpServletRequestImpl request) {
        HttpSessionImpl session = getSession(request);
        if (session != null) {
            session.pauseTimeoutTask();
        }
    }

    @Override
    public boolean isRequestedSessionIdValid(HttpServletRequestImpl request) {
        HttpSessionImpl session = getSession(request);
        if (session == null) {
            return false;
        }
        return session.getId().equals(request.getRequestedSessionId());
    }

    private HttpSessionImpl getSession(HttpServletRequestImpl request) {
        String sessionId = request.getActualSessionId();
        if (sessionId == null) {
            sessionId = request.getRequestedSessionId();
        }
        if (sessionId == null) {
            return null;
        }
        HttpSessionImpl session = sessionMap.get(sessionId);
        if (session == null && StringUtils.isNotBlank(request.getRequestedSessionId()) && validateSessionId(request.getRequestedSessionId())) {
            throw new WafException(HttpStatus.FORBIDDEN);
        }
        if (session == null || session.isInvalid()) {
            return null;
        }
        session.pauseTimeoutTask();
        return session;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public HashedWheelTimer getTimer() {
        return timer;
    }

    private static String createSessionId() {
        Random random = new Random();
        //掩码+固定前缀+时间戳
        byte[] bytes = new byte[maskLength + DEFAULT_BYTES.length + Integer.BYTES];

        for (int i = 0; i < maskLength; ) {
            for (int rnd = random.nextInt(), n = Math.min(maskLength - i, Integer.SIZE / Byte.SIZE); n-- > 0; rnd >>= Byte.SIZE)
                bytes[i++] = (byte) rnd;
        }
        System.arraycopy(DEFAULT_BYTES, 0, bytes, maskLength, DEFAULT_BYTES.length);
        //将System.nanoTime()填充至bytes后四字节
        int time = (int) System.nanoTime();
        bytes[maskLength + DEFAULT_BYTES.length] = (byte) (time >>> 24);
        bytes[maskLength + DEFAULT_BYTES.length + 1] = (byte) (time >>> 16);
        bytes[maskLength + DEFAULT_BYTES.length + 2] = (byte) (time >>> 8);
        bytes[maskLength + DEFAULT_BYTES.length + 3] = (byte) (time);

        for (int i = maskLength; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ bytes[i % maskLength]);
        }

        return MAGIC_NUMBER + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean validateSessionId(String sessionId) {
        if (!sessionId.startsWith(MAGIC_NUMBER)) {
            LOGGER.warn("invalid sessionId:{},ignore", sessionId);
            //检测到非法会话
            return false;
        }
        byte[] bytes = Base64.getUrlDecoder().decode(sessionId.substring(2));
        int maskLimit = maskLength + DEFAULT_BYTES.length;
        for (int i = maskLength; i < bytes.length; i++) {
            bytes[i] = (byte) (bytes[i] ^ bytes[i % maskLength]);
            if (i < maskLimit && bytes[i] != DEFAULT_BYTES[i - maskLength]) {
                return true;
            }
        }
//        System.out.println(new String(bytes, maskLength, bytes.length - maskLength - 4));
//        int time = (bytes[bytes.length - 4] & 0xFF) << 24 | (bytes[bytes.length - 3] & 0xFF) << 16 | (bytes[bytes.length - 2] & 0xFF) << 8 | (bytes[bytes.length - 1] & 0xFF);
        return false;
    }
}
