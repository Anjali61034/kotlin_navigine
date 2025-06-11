package com.example.canary.fragments.utils

object Constants {

    const val TAG = "NavigineDemo.LOG"

    // deep link query params
    const val DL_QUERY_SERVER = "server"
    const val DL_QUERY_USERHASH = "userhash"
    const val DL_QUERY_LOCATION_ID = "loc"
    const val DL_QUERY_SUBLOCATION_ID = "subloc"
    const val DL_QUERY_VENUE_ID = "venue_id"

    // notifications
    const val NOTIFICATION_CHANNEL_ID = "com.example.canary.PUSH"
    const val NOTIFICATION_CHANNEL_NAME = "NAVIGINE_PUSH"
    const val NOTIFICATION_PUSH_ID = 1
    const val REQUEST_CODE_NOTIFY = 102

    // notifications extras
    const val NOTIFICATION_TITLE = "notification_title"
    const val NOTIFICATION_TEXT = "notification_text"
    const val NOTIFICATION_IMAGE = "notification_image"

    // network
    const val HOST_VERIFY_TAG = "verify_request"
    const val ENDPOINT_HEALTH_CHECK = "/mobile/health_check"
    const val ENDPOINT_GET_USER = "/mobile/v1/users/get?userHash="
    const val RESPONSE_KEY_NAME = "name"
    const val RESPONSE_KEY_EMAIL = "email"
    const val RESPONSE_KEY_HASH = "hash"
    const val RESPONSE_KEY_AVATAR = "avatar_url"
    const val RESPONSE_KEY_COMPANY = "company_name"

    // anim image sizes
    const val SIZE_SUCCESS = 52
    const val SIZE_FAILED = 32
    const val CHECK_FRAME_SELECTED = 1f

    // broadcast events
    const val LOCATION_CHANGED = "LOCATION_CHANGED"
    const val VENUE_SELECTED = "VENUE_SELECTED"
    const val VENUE_FILTER_ON = "VENUE_FILTER_ON"
    const val VENUE_FILTER_OFF = "VENUE_FILTER_OFF"

    // intent keys
    const val KEY_VENUE_SUBLOCATION = "venue_sublocation"
    const val KEY_VENUE_POINT = "venue_point"
    const val KEY_VENUE_CATEGORY = "venue_category"

    // debug mode
    const val LIST_SIZE_DEFAULT = 6

    // circular progress
    const val CIRCULAR_PROGRESS_DELAY_SHOW = 200
    const val CIRCULAR_PROGRESS_DELAY_HIDE = 700
}