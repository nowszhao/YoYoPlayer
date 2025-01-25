package `fun`.coda.app.yoyoplayer.utils

import android.content.Context
import android.content.SharedPreferences

class CookieManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
    
    fun saveCookie(cookie: String) {
        prefs.edit().putString(COOKIE_KEY, cookie).apply()
    }
    
    fun getCookie(): String {
        val defaultCookies = "buvid_fp_plain=undefined; DedeUserID=401459154; DedeUserID__ckMd5=34f27959b95efc08; header_theme_version=CLOSE; buvid4=8704A773-5351-EEAD-E3D2-E7437ACF6D2178765-022072203-%2BgCBhWQVL858xEUflQm9wQ%3D%3D; enable_web_push=DISABLE; rpdid=0zbfAI3tap|Pnx6Nvfu|4zQ|3w1RjoCH; PVID=1; home_feed_column=5; FEED_LIVE_VERSION=V_HEADER_LIVE_NO_POP; _uuid=BB5786C2-104B1-CEE3-13FA-7D7CBFA32210168900infoc; buvid3=A92D6785-36DE-FBCB-6207-28BC0E4EB93341767infoc; b_nut=1724924541; hit-dyn-v2=1; browser_resolution=1728-860; enable_feed_channel=DISABLE; historyviewmode=list; fingerprint=62a3799f89631686ead05631b4b1fb47; buvid_fp=62a3799f89631686ead05631b4b1fb47; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3MzgwMjk4MzgsImlhdCI6MTczNzc3MDU3OCwicGx0IjotMX0.3UP0_G9G1c1AndpBGikqaOGnwwt0wWjC5MzCpAJMEac; bili_ticket_expires=1738029778; CURRENT_QUALITY=32; SESSDATA=54792749%2C1753325335%2Cbd73b%2A12CjBscuIx7bg4UhzUuPJ0hbTZQ9QwmducmKKYbMdQhJ7WCkEnDkHi8L4MzawKBsjjUsISVk1zZWFxam5JOThjYTl5NjFJbVUxeVRlWldpLWFZY090bzJBVjFGa2hrX1pQc3BVZHZLVzdVcVZCTE5NT1VSUUZhcGt6bUwwcHhVYnpZUnk1SXFJV29RIIEC; bili_jct=5430e5b65515a1a4813b0f7633ad91c9; sid=81gyub3r; bp_t_offset_401459154=1026266131683344384; b_lsid=DF2710593_1949D93FE6C; CURRENT_FNVAL=4048"

        return prefs.getString(COOKIE_KEY, defaultCookies) ?: defaultCookies
    }
    
    fun hasCookie(): Boolean {
        return getCookie().isNotEmpty()
    }
    
    companion object {
        private const val COOKIE_KEY = "bili_cookie"
    }
} 