package io.molfar.workshop1

import io.molfar.workshop1.Validators.optionalInput


//typealias Subscriber<T> = (T) -> Unit
//
//open class Signal<T> {
//
//    private val subscriptions: MutableMap<UUID, Subscriber<T>> = mutableMapOf()
//
//    protected fun notifyObservers(t: T) {
//        subscriptions.forEach { (_, v) ->
//            v.invoke(t)
//        }
//    }
//
//    open fun observe(block: (T) -> Unit): Subscription {
//        val subscription = UUID.randomUUID()
//        subscriptions[subscription] = block
//        return Subscription {
//            removeObserver(subscription)
//        }
//    }
//
//    private fun removeObserver(subscription: UUID) {
//        subscriptions.remove(subscription)
//    }
//
//}
//
//class Pipe<T> : Signal<T>() {
//
//    fun send(t: T) {
//        notifyObservers(t)
//    }
//
//}
//
//class Observable<T>(value: T) : Signal<T>() {
//
//    var value: T = value
//        set(value) {
//            field = value
//            notifyObservers(field)
//        }
//
//    override fun observe(block: (T) -> Unit): Subscription {
//        val subscription = super.observe(block)
//        block.invoke(value)
//        return subscription
//    }
//}
//
//class Subscription(dispose: () -> Unit)
//
//open class InputField(val isValid: Observable<Boolean> = Observable(true))
//
//class Form {
//
//    var inputFields: List<InputField> = emptyList()
//        set(value) {
//            field = value
//            subscribeForFieldsValidity()
//        }
//
//    val isValid = Observable(false)
//
//    private fun subscribeForFieldsValidity() {
////        isValid.value =  inputFields.any { !it.isValid.value }.not()
//        inputFields.forEach { field ->
//            field.isValid.observe {
//                updateValidity()
//            }
//        }
//    }
//
//    private fun updateValidity() {
//        isValid.value = inputFields
//            .map { it.isValid.value }
//            .reduce { acc, b -> acc && b }
//    }
//}

//class TextInputField(
//    var validator: Validator<String> = Validator { true }
//) : InputField() {
//    var text: Observable<String> = Observable("")
//
//    init {
//        text.observe {
//            isValid.value = validator.check.invoke(it)
//        }
//    }
//}

class Validator<T>(val check: (T) -> Validated<T>)

fun <U, T> Validator<T>.flatMap(block: (U) -> T): Validator<U> {
    return Validator {
        val result = this.check.invoke(block.invoke(it))
        if (result.isValid()) Validated.Valid(it)
        else Validated.NotValid(result.errors)
    }
}


//fun <T> no(inputValidator: Validator<T>): Validator<T> {
//    return Validator<T> {
//        inputValidator.check.invoke(it).not()
//    }
//}

object Validators {
//    fun divisibleBy(input: Int): Validator<Int> = Validator { it % input == 0 }

    fun greaterThan(input: Int, error: String): Validator<Int> = Validator {
        if (it > input) Validated.Valid(it)
        else Validated.NotValid(listOf(error))
    }


    fun smallerThan(input: Int, error: String): Validator<Int> = Validator {
        if (it < input) Validated.Valid(it)
        else Validated.NotValid(listOf(error))
    }

    fun smallerThanOrEqual(input: Int, error: String): Validator<Int> =
        greaterThan(input, "").not(error)

    fun greaterThanOrEqual(input: Int, error: String): Validator<Int> =
        smallerThan(input, "").not(error)

    fun isTextLongerThan(minLength: Int, error: String): Validator<String> =
        greaterThan(minLength, error).flatMap { it.length }

    fun equals(input: Int, error: String): Validator<Int> = Validator {
        if (it == input) Validated.Valid(it)
        else Validated.NotValid(listOf(error))
    }

    fun optionalInput(): Validator<String> = equals(0, "Error").flatMap { it.length }
}


sealed class Validated<T> {
    fun isValid() = this is Valid

    val errors: List<String>
        get() = when (this) {
            is NotValid -> this._errors
            else -> emptyList()
        }

    data class Valid<T>(val value: T) : Validated<T>()
    data class NotValid<T>(val _errors: List<String>) : Validated<T>()
}

fun <T> Validator<T>.and(validator: Validator<T>): Validator<T> {
    return Validator<T> {
        val result1 = this.check.invoke(it)
        val result2 = validator.check.invoke(it)
        val bothValid = result1.isValid() && result2.isValid()
        if (bothValid) Validated.Valid(it)
        else Validated.NotValid(result1.errors + result2.errors)
    }
}


fun <T> Validator<T>.or(validator: Validator<T>): Validator<T> {

    return Validator<T> {
        val result1 = this.check.invoke(it)
        if (result1.isValid()) return@Validator result1
        validator.check.invoke(it)
    }
}

fun <T> Validator<T>.not(error: String): Validator<T> {
    return Validator<T> {
        if (this.check(it).isValid()) Validated.NotValid(listOf(error))
        else Validated.Valid(it)
    }
}

//fun <T> Validator<T>.xor(validator: Validator<T>): Validator<T> {
//
//    return Validator<T> {
//        this.check.invoke(it).xor(validator.check.invoke(it))
//    }
//}

fun main() {

    val validator1 = Validators.isTextLongerThan(2, "Must be longer than 2")
//    <Int> {
//        if (it == 1) Validated.Valid(it)
//        else Validated.NotValid(listOf("Must be 1"))
//    }
    val validator2 = Validators.isTextLongerThan(10, "").not("Must be shorter than 10")
//        Validator<Int> {
//        if (it == 2) Validated.Valid(it)
//        else Validated.NotValid(listOf("Must be 2"))
//    }

    val checkPassword = validator1.and(validator2)
    val validator3 = checkPassword.or(optionalInput())

    println(validator3.check("123123"))
    println(validator3.check("12112"))
    println(validator3.check("12"))
    println(validator3.check("121212121212121211212"))
    println(validator3.check(""))
//    val email = TextInputField()
//    val password = TextInputField()
//
//    val form = Form()
//    form.inputFields = listOf(email, password)
//
//    form.isValid.observe {
//        println(it)
//    }
//    email.validator = Validator {
//        it.contains("@")
//    }
//    password.validator = Validator {
//        it.length > 5
//    }
//
//    email.text.value = ""
//    email.text.value = "whjef"
//    password.text.value = "whjef"
//    password.text.value = ""
//    email.text.value = "whj@ef"
//    password.text.value = "whjefefeef"


//    val signal = Observable(5)
//
//    signal.observe {
//        println(it)
//    }
//    signal.value = 10

//    signal.notifyObservers(5)

    //val validator1 = Validator<Int>().divisibleBy()
//    val validator1 = Validators.divisibleBy(2)
//    val validator2 = Validators.divisibleBy(3)
//    val validator3 = validator1.and(validator2)
//    println(validator3.check.invoke(6))

    //Validators.divisibleBy(2).and(Validators.divisibleBy(3))
//    with(Validators) {
//        val resultValidator = divisibleBy(2).and(divisibleBy(3))
//    }


//    val passwordValidator = greaterThanOrEqual(6).and(smallerThan(20))
//    println("first result ${passwordValidator.check}")
//
//    listOf(5, 6, 19, 20).map {
//        passwordValidator.check.invoke(it)
//    }.forEach {
//        println(it)
//    }

    /**
    last test
     */
//    val passwordValidator = isTextLongerThan(3).and(no(isTextLongerThan(10)))
//    println("first result ${passwordValidator.check}")
//
//    listOf("123", "123456", "12", "12312312321").map {
//        passwordValidator.check.invoke(it)
//    }.forEach {
//        println(it)
//    }


}