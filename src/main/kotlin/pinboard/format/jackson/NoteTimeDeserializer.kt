package pinboard.format.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import pinboard.format.FormatUtils
import java.util.*

/**
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
class NoteTimeDeserializer(vc: Class<*>?) : StdDeserializer<Date>(vc) {

    private constructor() : this(Date::class.java)

    override fun deserialize(jp: JsonParser?,
                             ctx: DeserializationContext?): Date {
        val codec = jp!!.codec
        val jsonNode: JsonNode = codec.readTree(jp)
        val stringVal = jsonNode.asText()
        return FormatUtils.decodeNoteDate(stringVal)
    }
}