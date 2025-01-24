package `fun`.coda.app.yoyoplayer.utils

import android.content.Context
import android.content.SharedPreferences

class CookieManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)
    
    fun saveCookie(cookie: String) {
        prefs.edit().putString(COOKIE_KEY, cookie).apply()
    }
    
    fun getCookie(): String {
        return prefs.getString(COOKIE_KEY, "") ?: "buvid_fp_plain=undefined; DedeUserID=401459154; DedeUserID__ckMd5=34f27959b95efc08; header_theme_version=CLOSE; buvid4=8704A773-5351-EEAD-E3D2-E7437ACF6D2178765-022072203-%2BgCBhWQVL858xEUflQm9wQ%3D%3D; enable_web_push=DISABLE; rpdid=0zbfAI3tap|Pnx6Nvfu|4zQ|3w1RjoCH; PVID=1; home_feed_column=5; FEED_LIVE_VERSION=V_HEADER_LIVE_NO_POP; CURRENT_QUALITY=0; _uuid=BB5786C2-104B1-CEE3-13FA-7D7CBFA32210168900infoc; buvid3=A92D6785-36DE-FBCB-6207-28BC0E4EB93341767infoc; b_nut=1724924541; hit-dyn-v2=1; browser_resolution=1728-860; bili_ticket=eyJhbGciOiJIUzI1NiIsImtpZCI6InMwMyIsInR5cCI6IkpXVCJ9.eyJleHAiOjE3Mzc3NzIwNjcsImlhdCI6MTczNzUxMjgwNywicGx0IjotMX0.XCIkpd5L3Gbw4XUVa8GkrB32VNJstZNjdsdMf6gH3us; bili_ticket_expires=1737772007; SESSDATA=a6abb816%2C1753064868%2C5b35e%2A12CjD3LUznIbjfdfWC6bYh7HKvCK4kiA4b8w8rHeFkdlX4GYUPND7fIbED3yyRg3H0Iy8SVnNXSTBXdkhMRDE0VV81S1Q0WHluSnBtcXhMYndUaG04SmZja1RJREJRTlItRW5fdDAyYjV4c3pJbUhUemFzelNXbXFOR2dzU0RLWEJfNkFGc3MxVzFnIIEC; bili_jct=02f7da1c2e1fb8ac82c01f5e498e30b6; sid=4qpea3di; enable_feed_channel=DISABLE; historyviewmode=list; bsource=search_google; fingerprint=62a3799f89631686ead05631b4b1fb47; bp_t_offset_401459154=1025935651129786368; buvid_fp=62a3799f89631686ead05631b4b1fb47; CURRENT_FNVAL=4048; b_lsid=C41DF1085_19497BF6DB0"
    }
    
    fun hasCookie(): Boolean {
        return getCookie().isNotEmpty()
    }
    
    companion object {
        private const val COOKIE_KEY = "bili_cookie"
    }
} 