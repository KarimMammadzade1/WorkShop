package io.molfar.workshop1

import io.molfar.workshop1.Validators.greaterThanOrEqual
import io.molfar.workshop1.Validators.isTextLongerThan
import io.molfar.workshop1.Validators.smallerThan
import java.util.*

typealias Subscriber<T> = (T) -> Unit

open class Signal<T> {

    private val subscriptions: MutableMap<UUID, Subscriber<T>> = mutableMapOf()

    protected fun notifyObservers(t: T) {
        subscriptions.forEach { (_, v) ->
            v.invoke(t)
        }
    }

    open fun observe(block: (T) -> Unit): Subscription {
        val subscription = UUID.randomUUID()
        subscriptions[subscription] = block
        return Subscription {
            removeObserver(subscription)
        }
    }

    private fun removeObserver(subscription: UUID) {
        subscriptions.remove(subscription)
    }

}

class Pipe<T> : Signal<T>() {

    fun send(t: T) {
        notifyObservers(t)
    }

}

class Observable<T>(value: T) : Signal<T>() {

    var value: T = value
        set(value) {
            field = value
            notifyObservers(field)
        }

    override fun observe(block: (T) -> Unit): Subscription {
        val subscription = super.observe(block)
        block.invoke(value)
        return subscription
    }
}

class Subscription(dispose: () -> Unit)

open class InputField(val isValid: Observable<Boolean> = Observable(true))

class Form {

    var inputFields: List<InputField> = emptyList()
        set(value) {
            field = value
            subscribeForFieldsValidity()
        }

    val isValid = Observable(false)

    private fun subscribeForFieldsValidity() {
//        isValid.value =  inputFields.any { !it.isValid.value }.not()
        inputFields.forEach { field ->
            field.isValid.observe {
                updateValidity()
            }
        }
    }

    private fun updateValidity() {
        isValid.value = inputFields
            .map { it.isValid.value }
            .reduce { acc, b -> acc && b }
    }
}

class TextInputField(
    var validator: Validator<String> = Validator { true }
) : InputField() {
    var text: Observable<String> = Observable("")

    init {
        text.observe {
            isValid.value = validator.check.invoke(it)
        }
    }
}

class Validator<T>(val check: (T) -> Boolean)

fun <U, T> Validator<T>.flatMap(block: (U) -> T): Validator<U> {
    return Validator<U> {
        this.check.invoke(block.invoke(it))
    }
}


fun <T> no(inputValidator: Validator<T>): Validator<T> {
    return Validator<T> {
        inputValidator.check.invoke(it).not()
    }
}

object Validators {
    fun divisibleBy(input: Int): Validator<Int> = Validator { it % input == 0 }

    fun greaterThan(input: Int): Validator<Int> = Validator { it > input }

    fun smallerThan(input: Int): Validator<Int> = Validator { it < input }

    fun smallerThanOrEqual(input: Int): Validator<Int> = greaterThan(input).not()

    fun greaterThanOrEqual(input: Int): Validator<Int> = smallerThan(input).not()

    fun isTextLongerThan(minLength: Int): Validator<String> =
        greaterThan(minLength).flatMap { it.length }

}


fun <T> Validator<T>.and(validator: Validator<T>): Validator<T> {

    return Validator<T> {
        this.check.invoke(it) && validator.check.invoke(it)
    }
}

fun <T> Validator<T>.or(validator: Validator<T>): Validator<T> {

    return Validator<T> {
        this.check.invoke(it) || validator.check.invoke(it)
    }
}

fun <T> Validator<T>.not(): Validator<T> {

    return Validator<T> {
        !this.check.invoke(it)
    }
}

fun <T> Validator<T>.xor(validator: Validator<T>): Validator<T> {

    return Validator<T> {
        this.check.invoke(it).xor(validator.check.invoke(it))
    }
}

fun main() {

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