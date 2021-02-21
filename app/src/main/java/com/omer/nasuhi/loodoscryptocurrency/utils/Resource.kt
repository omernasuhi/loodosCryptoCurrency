package  com.omer.nasuhi.loodoscryptocurrency.utils

data class Resource<out T>(var status: Status?, val data: T?, val message: String?) {

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object {
        fun <T> Success(data: T): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }

        fun <T> Error(message: String, data: T? = null): Resource<T> {
            return Resource(Status.ERROR, data, message)
        }

        fun <T> Loading(data: T? = null): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }
    }
}