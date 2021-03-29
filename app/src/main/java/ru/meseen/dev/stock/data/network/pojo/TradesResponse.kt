package ru.meseen.dev.stock.data.network.pojo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class TradesResponse(

	/**
	 * List of trades or price updates.
	 */
	@SerialName("data")
	val data: List<DataItem?>? = null,

	/**
	 * Message type.
	 */
	@SerialName("type")
	val type: String? = null
)

@Serializable
data class DataItem(

	@SerialName("p")
	val price: Double? = null,

	/**
	 * List of trade conditions. A comprehensive list of trade conditions code can be found
	 * @see <a href="https://docs.google.com/spreadsheets/d/1PUxiSWPHSODbaTaoL2Vef6DgU-yFtlRGZf19oBb9Hp0/edit#gid=0">here</a>
	 */
	@SerialName("c")
	val conditions: List<String?>? = null,

	@SerialName("s")
	val symbol: String? = null,

	/**
	 * UNIX milliseconds timestamp.
	 */
	@SerialName("t")
	val time: Long? = null,

	@SerialName("v")
	val volume: Double? = null
)
