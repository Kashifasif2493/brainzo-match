package com.brainzomatch.app.core.parentgate

import kotlin.random.Random

/**
 * Adult two-digit math challenge used as a COPPA Parental Verification Gate
 * prior to accessing in-app purchases, billing screens, or external links.
 * File Path: app/src/main/java/com/brainzomatch/app/core/parentgate/ParentalVerification.kt
 */
data class ParentalMathChallenge(
    val factorA: Int,
    val factorB: Int,
    val expectedAnswer: Int,
    val displayPrompt: String
)

object ParentalVerification {

    /**
     * Generates an adult multiplication question (e.g. 14 × 17 = 238)
     * difficult for young children (ages 5–12) to solve instantly without adult assistance.
     */
    fun generateAdultVerificationQuestion(): ParentalMathChallenge {
        val numA = Random.nextInt(12, 19)
        val numB = Random.nextInt(7, 14)
        val answer = numA * numB
        return ParentalMathChallenge(
            factorA = numA,
            factorB = numB,
            expectedAnswer = answer,
            displayPrompt = "Adults Only: What is $numA × $numB ?"
        )
    }

    /**
     * Validates whether the user's input matches the challenge answer.
     */
    fun verifyAdultInput(input: String, challenge: ParentalMathChallenge): Boolean {
        val parsed = input.trim().toIntOrNull() ?: return false
        return parsed == challenge.expectedAnswer
    }


    /** Short name used by the Parent Dashboard and VIP screens. */
    fun generateChallenge(): ParentalMathChallenge = generateAdultVerificationQuestion()

    /** Checks a numeric answer against the challenge. */
    fun verifyAnswer(answer: Int, challenge: ParentalMathChallenge): Boolean =
        answer == challenge.expectedAnswer

    /**
     * Verifies against a 4-digit numeric parental PIN.
     */
    fun verifyPin(enteredPin: String, storedHash: String): Boolean {
        return hashPin(enteredPin) == storedHash
    }

    /**
     * SHA-256 hash calculation for parent PIN persistence.
     */
    fun hashPin(pin: String): String {
        val bytes = java.security.MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
