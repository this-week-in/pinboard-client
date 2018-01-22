package pinboard.format

import org.apache.commons.logging.LogFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
object FormatUtils {

    private val log = LogFactory.getLog(javaClass)

    private val decodeDateFormat = utcAwareDateFormatter("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val encodeDateFormat = utcAwareDateFormatter("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val decodeNoteDateFormat = utcAwareDateFormatter("yyyy-MM-dd HH:mm:ss")
    private val encodeNoteDateFormat = utcAwareDateFormatter("yyyy-MM-dd HH:mm:ss")

    fun encodeNoteDate(d: Date): String {
        return encodeNoteDateFormat.format(d)
    }

    fun decodeNoteDate(noteDate: String): Date {
        log.debug("decodeNoteDate: $noteDate")
        return decodeNoteDateFormat.parse(noteDate)
    }

    fun encodeDate(d: Date): String {
        return encodeDateFormat.format(d)
    }

    fun decodeDate(date: String): java.util.Date? {
        log.debug("decode date: $date")
        return decodeDateFormat.parse(date)
    }

    fun encodeYesOrNo(b: Boolean): String {
        return if (b) "yes" else "no"
    }

    fun decodeYesOrNo(yesOrNoStr: String): Boolean {
        return yesOrNoStr.toLowerCase().trim().contentEquals("yes")
    }

    private fun utcAwareDateFormatter(sdfString: String): SimpleDateFormat {
        val sdf = SimpleDateFormat(sdfString)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf
    }


}
