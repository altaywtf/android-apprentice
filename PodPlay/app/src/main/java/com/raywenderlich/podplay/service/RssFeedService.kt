package com.raywenderlich.podplay.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.w3c.dom.Node
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Url
import javax.xml.parsers.DocumentBuilderFactory

interface FeedService {
    @Headers(
        "Content-Type: application/xml; charset=utf-8",
        "Accept: application/xml"
    )
    @GET
    suspend fun getFeed(@Url xmlFileURL: String): Response<ResponseBody>
}

class RssFeedService private constructor() {
    private fun domToRssFeedResponse(node: Node, rssFeedResponse: RssFeedResponse) {
        if (node.nodeType == Node.ELEMENT_NODE) {
            if (node.parentNode.nodeName == "channel") {
                when (node.nodeName) {
                    "title" -> rssFeedResponse.title = node.textContent
                    "description" -> rssFeedResponse.description = node.textContent
                    "itunes:summary" -> rssFeedResponse.summary = node.textContent
                    "item" -> rssFeedResponse.episodes?.add(RssFeedResponse.EpisodeResponse())
                    "pubDate" -> rssFeedResponse.lastUpdated = DateUtils.xmlDateToDate(node.textContent)
                }
            }

            if (node.parentNode.nodeName == "item" && node.parentNode.parentNode?.nodeName == "channel") {
                val currentItem = rssFeedResponse.episodes?.last()
                if (currentItem != null) {
                    when (node.nodeName) {
                        "title" -> currentItem.title = node.textContent
                        "description" -> currentItem.description = node.textContent
                        "itunes:duration" -> currentItem.duration = node.textContent
                        "guid" -> currentItem.guid = node.textContent
                        "pubDate" -> currentItem.pubDate = node.textContent
                        "link" -> currentItem.link = node.textContent
                        "enclosure" -> {
                            currentItem.url = node.attributes.getNamedItem("url").textContent
                            currentItem.type = node.attributes.getNamedItem("type").textContent
                        }
                    }
                }
            }
        }

        val nodeList = node.childNodes
        for (i in 0 until nodeList.length) {
            val childNode = nodeList.item(i)
            domToRssFeedResponse(childNode, rssFeedResponse)
        }
    }

    suspend fun getFeed(xmlFileURL: String): RssFeedResponse? {
        val retrofit = Retrofit.Builder()
            .baseUrl("${xmlFileURL.split("?")[0]}/")
            .build()

        val service = retrofit.create(FeedService::class.java)

        try {
            val result = service.getFeed(xmlFileURL)
            if (result.code() >= 400) {
                return null
            }

            val responseBody = result.body() ?: return null

            var rssFeedResponse: RssFeedResponse? = null
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            val documentBuilder = documentBuilderFactory.newDocumentBuilder()

            withContext(Dispatchers.IO) {
                val doc = documentBuilder.parse(responseBody.byteStream())
                val rss = RssFeedResponse(episodes = mutableListOf())
                domToRssFeedResponse(doc, rss)
                rssFeedResponse = rss
            }

            return rssFeedResponse
        } catch (t: Throwable) {
            println("error, ${t.localizedMessage}")
        }

        return null
    }

    companion object {
        val instance: RssFeedService by lazy {
            RssFeedService()
        }
    }
}
