package com.sparkout.chat.network

import javax.inject.Inject

/**
 *Created by Nivetha S on 31-01-2022.
 */
class ApiGpsComHelperImpl @Inject constructor(private val apiTerService: ApiGPsComChatService) :
    BaseDataSource() {


  /*  suspend fun getCountryCommonDetails(
        country: String,
        state: String,
        city: String
    ): Response<CountryStateCityResponse> =
        apiTerService.getAddressDetails(country, state, city)

    suspend fun getCommonDetails(): Response<CommonResponseData> =
        apiTerService.getCountryList()

    suspend fun getCommonDetailsState(countryId: String): Response<CommonResponseData> =
        apiTerService.getStateList(countryId)

    suspend fun getCommonDetailsCity(stateId: String): Response<CityResponseData> =
        apiTerService.getCityList(stateId)*/



   /* suspend fun storeManualRetirementAsset(storeRetirementAssetRequest: StoreRetirementAssetRequest) =
        getResult { apiTerService.storeManualRetirementAsset(storeRetirementAssetRequest) }
*/




}