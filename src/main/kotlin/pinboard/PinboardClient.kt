package pinboard

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import pinboard.format.FormatUtils
import pinboard.format.jackson.*
import java.util.*
import kotlin.collections.HashMap

/**
 * This is a client for <a href="https://pinboard.in/api">the Pinboard API</a>.
 *
 * @author <a href="mailto:josh@joshlong.com">Josh Long</a>
 */
open class PinboardClient(private var token: String,
                          private val rt: RestTemplate? = null) {

	private val restTemplate = RestTemplateBuilder()
			.additionalCustomizers(RestTemplateCustomizer {
				val messageConverter = it.messageConverters.find { it is AbstractJackson2HttpMessageConverter }
						as AbstractJackson2HttpMessageConverter
				val mts = messageConverter.supportedMediaTypes
				val parseMediaType = MediaType.parseMediaType("text/plain;charset=utf-8")
				val listOfMts = ArrayList<MediaType>()
				listOfMts.addAll(mts)
				listOfMts.add(parseMediaType)
				messageConverter.supportedMediaTypes = listOfMts
			})
			.configure(rt ?: RestTemplate())

	private val pinboardApiEndpoint = "https://api.pinboard.in/v1"

	open fun addPost(url: String,
	                 description: String,
	                 extended: String,
	                 tags: Array<String>,
	                 dt: Date,
	                 replace: Boolean,
	                 shared: Boolean,
	                 toread: Boolean): Boolean {
		val params = exchangeParameters("url" to url, "description" to description, "extended" to extended,
				"tags" to tags, "dt" to dt, "replace" to replace, "shared" to shared, "toread" to toread)
		val result: ResponseEntity<String> = exchange("/posts/add", params, object : ParameterizedTypeReference<String>() {})
		return isDone(result)
	}

	open fun deletePost(url: String): Boolean {
		val params = exchangeParameters("url" to url)
		return isDone(exchange("/posts/delete", params, object : ParameterizedTypeReference<String>() {}))
	}

	open fun getAllPosts(tag: Array<String>,
	                start: Int = 0,
	                results: Int = -1,
	                fromdt: Date? = null,
	                todt: Date? = null,
	                meta: Int = 0): Array<Bookmark> {
		assert(tag.size <= 3, { "there should be no more than three tag" })
		assert(tag.isNotEmpty(), { "there should be at least one tag" })
		val parmMap = exchangeParameters("tag" to tag, "start" to start, "results" to results, "fromdt" to fromdt, "todt" to todt, "meta" to meta)
		return exchange("/posts/all", parmMap, object : ParameterizedTypeReference<Array<Bookmark>>() {}).body
	}

	open fun getPosts(url: String? = null, dt: Date? = null, tag: Array<String>? = null, meta: Boolean? = null): Bookmarks {
		assert(dt != null || url != null, { "you must specify either the date or the URL" })
		val parameters = exchangeParameters(
				"tag" to tag,
				"url" to url,
				"meta" to meta,
				"dt" to dt)
		return exchange("/posts/get", parameters, object : ParameterizedTypeReference<Bookmarks>() {}).body
	}

	open fun getRecentPosts(tag: Array<String>? = null, count: Int? = 15): Bookmarks {
		val parameters = exchangeParameters("tag" to tag, "count" to count)
		return exchange("/posts/recent", parameters, object : ParameterizedTypeReference<Bookmarks>() {}).body
	}

	open fun getNoOfPostsByDate(tag: Array<String>): PostsByDate {
		val params = exchangeParameters("tag" to tag);
		return exchange("/posts/dates", params, object : ParameterizedTypeReference<PostsByDate>() {}).body
	}

	open fun suggestTagsForPost(url: String): SuggestedTags {
		val params = exchangeParameters("url" to url)
		val result = exchange("/posts/suggest", params, object : ParameterizedTypeReference<Array<Map<String, Array<String>>>>() {})
		val body: Array<Map<String, Array<String>>> = result.body
		val popular = "popular"
		val recommended = "recommended"
		val popularStrings = body.first { it.containsKey(popular) }[popular]
		val recommendedStrings = body.first { it.containsKey(recommended) }[recommended]
		return SuggestedTags(popularStrings, recommendedStrings)
	}

	open fun getUserTags(): Map<String, Int> {
		return exchange("/tags/get", exchangeParameters(), object : ParameterizedTypeReference<Map<String, Int>>() {}).body
	}

	private fun <T> exchange(incomingUrl: String,
	                         paramMap: Map<String, String>,
	                         ptr: ParameterizedTypeReference<T>?): ResponseEntity<T> {
		val params = defaultParameters(paramMap)
		val map = params.entries.map { "${it.key}={${it.key}}" }
		val paramString = map.joinToString("&")
		val url = "${pinboardApiEndpoint}${incomingUrl}?${paramString}"
		return restTemplate.exchange(url, HttpMethod.GET, null, ptr, params)
	}

	private fun isDone(result: ResponseEntity<String>): Boolean {
		return result.body.contains("done") && result.statusCode == HttpStatus.OK
	}

	private fun exchangeParameters(vararg pairs: Pair<String, Any?>): Map<String, String> {

		val i = pairs.toMap(LinkedHashMap(pairs.size))

		val o = java.util.HashMap<String, String>()
		o.put("auth_token", token)
		o.put("format", "json")

		val dateTransformer = { d: Date -> FormatUtils.encodeDate(d) }
		val tagTransformer = { t: Array<String> -> t.joinToString(" ") }
		val intTransformer = { t: Int -> Integer.toString(t) }
		val boolTransformer = { b: Boolean -> if (b) "yes" else "no" }

		contributeParameter("url", i, o, null, null)
		contributeParameter("shared", i, o, null, boolTransformer)
		contributeParameter("toread", i, o, null, boolTransformer)
		contributeParameter("replace", i, o, null, boolTransformer)
		contributeParameter("extended", i, o, null, null)
		contributeParameter("dt", i, o, null, dateTransformer)
		contributeParameter("description", i, o, null, null)
		contributeParameter("tag", i, o, null, tagTransformer)
		contributeParameter("tags", i, o, null, tagTransformer)
		contributeParameter("start", i, o, null, intTransformer)
		contributeParameter("results", i, o, null, intTransformer)
		contributeParameter("meta", i, o, null, intTransformer)
		contributeParameter("fromdt", i, o, null, dateTransformer)
		contributeParameter("todt", i, o, null, dateTransformer)
		contributeParameter("count", i, o, null, intTransformer)

		return o
	}

	open fun deleteTag(s: String): Boolean {
		val result = exchange("/tags/delete", exchangeParameters("tag" to arrayOf(s)), object : ParameterizedTypeReference<String>() {}).body
		return (result.toLowerCase().contains("done"))
	}

	open fun getUserSecret(): String {
		return exchange("/user/secret", exchangeParameters(), object : ParameterizedTypeReference<Map<String, String>>() {}).body["result"]!!
	}

	open fun getApiToken(): String {
		return exchange("/user/api_token/", exchangeParameters(), object : ParameterizedTypeReference<Map<String, String>>() {}).body["result"]!!
	}

	open fun getUserNotes(): Notes {
		return exchange("/notes/list", exchangeParameters(), object : ParameterizedTypeReference<Notes>() {}).body
	}

	open fun getUserNote(id: String): Note {
		return exchange("/notes/" + id, exchangeParameters(), object : ParameterizedTypeReference<Note>() {}).body
	}

	private fun defaultParameters(parmMap: Map<String, Any?>): Map<String, Any?> {
		val mix = HashMap<String, Any?>()
		mix.putAll(mapOf())
		mix.putAll(parmMap)
		return mix
	}

	private fun <T> contributeParameter(key: String,
	                                    inputMap: MutableMap<String, Any?>,
	                                    outputMap: MutableMap<String, String>,
	                                    default: T? = null,
	                                    stringTransformer: ((T) -> String)? = null) {

		val t: T? = if (inputMap.containsKey(key)) inputMap[key] as T else default
		if (t != null) {
			if (stringTransformer != null) {
				outputMap.put(key, stringTransformer(t))
			} else {
				outputMap.put(key, t.toString())
			}
		}
	}
}

open class Note(var id: String?,
           var title: String?,
           var length: Int?,
           created: Date?,
           updated: Date?,
           var hash: String?) {

	@JsonSerialize(using = NoteTimeSerializer::class)
	@JsonDeserialize(using = NoteTimeDeserializer::class)
	@JsonProperty("created_at")
	var created: Date? = created

	@JsonSerialize(using = NoteTimeSerializer::class)
	@JsonDeserialize(using = NoteTimeDeserializer::class)
	@JsonProperty("updated_at")
	var updated: Date? = updated

	private constructor() : this(null, null, null, null, null, null)
}

open class Notes(var count: Int? = 0, var notes: Array<Note>?) {
	private constructor() : this(0, arrayOf())
}

open class SuggestedTags(val popular: Array<String>?, val recommended: Array<String>?) {
	private constructor() : this(arrayOf(), arrayOf())
}

open class Bookmarks(date: Date?, var user: String?, var posts: Array<Bookmark>) {

	private constructor() : this(null, null, emptyArray())

	@JsonSerialize(using = TimeSerializer::class)
	@JsonDeserialize(using = TimeDeserializer::class)
	var date: Date? = date
}

open class Bookmark(var href: String?,
               var description: String?,
               var extended: String?,
               var hash: String?,
               var meta: String?,
               time: java.util.Date?,
               shared: Boolean,
               toread: Boolean,
               tags: Array<String>) {

	private constructor() : this(null, null, null, null, null, null, false, false, emptyArray())

	@JsonSerialize(using = TagSerializer::class)
	@JsonDeserialize(using = TagDeserializer::class)
	var tags: Array<String> = tags

	@JsonDeserialize(using = TimeDeserializer::class)
	@JsonSerialize(using = TimeSerializer::class)
	var time: java.util.Date? = time

	@JsonDeserialize(using = YesNoDeserializer::class)
	@JsonSerialize(using = YesNoSerializer::class)
	var shared: Boolean = shared

	@JsonDeserialize(using = YesNoDeserializer::class)
	@JsonSerialize(using = YesNoSerializer::class)
	var toread: Boolean = toread

}

open class PostsByDate(var user: String?, tag: Array<String>?, val dates: Map<Date, Int>?) {

	private constructor() : this(null, emptyArray(), emptyMap())

	@JsonDeserialize(using = TagDeserializer::class)
	@JsonSerialize(using = TagSerializer::class)
	var tag = tag
}