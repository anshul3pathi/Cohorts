package com.example.cohorts.core.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.filters.MediumTest
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.model.User
import com.example.cohorts.core.succeeded
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltAndroidTest
@MediumTest
class CohortsRepositoryAndroidTest {

    private val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val hiltRule = HiltAndroidRule(this)

    @Inject lateinit var repository: CohortsRepository
    @Inject lateinit var auth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore

    @get:Rule
    var rule = RuleChain.outerRule(hiltRule).around(instantTaskExecutorRule)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun fetchCohortsQuery_returnsQueryForGettingCohorts() = runBlockingTest {
        val result = repository.fetchCohortsQuery()
        assertThat(result.succeeded, `is`(true))
    }


    @Test
    // You should be logged in for this test to pass
    fun registerCurrentUser_getCurrentUser_savesAndGetsCurrentlyLoggedInUser() = runBlocking {
        // Given - user is logged in
        // When -  user is registered
        val result = repository.registerCurrentUser()
        assertThat(result.succeeded, `is`(true))
        // Then - registered user should be the same as currently logged in user
        val user = repository.getCurrentUser()
        assertThat(user.succeeded, `is`(true))
        user as Result.Success
        assertThat(user.data.uid, `is`(auth.currentUser!!.uid))
    }

    @Test
    // You should be logged in for this test to pass
    fun getUserByEmail_givenUserEmail_getsUserInfo() = runBlocking {
        val email = "fakeemail@whatever.com"
        // Given - user with provided email exists in database
        // fake user data
        val user = User(
            uid = "fakeuid",
            userName = "fakeusername",
            userEmail = email
        )
        firestore.collection("users").document(user.uid!!).set(user).await()

        // When - this user is searched using provided email
        val searchedUser = repository.getUserByEmail(email)

        // delete fake user data from firestore
        firestore.collection("users").document(user.uid!!).delete().await()

        // Then - it should be same as the saved user
        assertThat(searchedUser.succeeded, `is`(true))
        searchedUser as Result.Success
        assertThat(searchedUser.data.uid!!, `is`(user.uid!!))
    }

    @Test
    fun saveCohort_savesTheGivenCohortInFirestoreDb() = runBlocking {
        // Given - the cohort to save
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
        )
        // When - cohort is saved
        repository.saveCohort(cohort)
        // Then - it should be saved in firestore
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get().await().toObject(Cohort::class.java)

        // Deleting the saved cohort
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        assertThat(savedCohort!!.cohortUid, `is`(cohort.cohortUid))
    }

    @Test
    fun getCohortById_givenCohortUID_returns_Cohort() = runBlocking {
        // Given - cohort is saved in database
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf(auth.currentUser!!.uid),
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()
        // When - cohort is searched by id
        val searchedCohort = repository.getCohortById(cohort.cohortUid)

        // Deleting saved cohort
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        // Then - it should return the saved cohort
        assertThat(searchedCohort.succeeded, `is`(true))
        searchedCohort as Result.Success
        assertThat(searchedCohort.data.cohortUid, `is`(cohort.cohortUid))
    }

    @Test
    fun deleteThisCohort_deletesTheGivenCohortFromFirestore() = runBlocking {
        // Given - cohort exists in firestore
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf(auth.currentUser!!.uid),
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()
        // When - this cohort is deleted
        val result = repository.deleteThisCohort(cohort)
        // Then - the given cohort should not exist in firestore
        assertThat(result.succeeded, `is`(true))
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get().await().toObject(Cohort::class.java)
        assertThat(savedCohort, `is`(nullValue()))
    }

    @Test
    fun addNewCohort_addsNewCohortToFirebase() = runBlocking {
        // Given - data for creating cohort
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc"
        )
        // When - new cohort is created
        val result = repository.addNewCohort(cohort)
        // Then - the given cohort should be added to firestore
        assertThat(result.succeeded, `is`(true))
        result as Result.Success

        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get().await().toObject(Cohort::class.java)

        // Deleting this cohort from firestore
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()
        // Deleting this cohort from the list of cohorts the logged in user is in
        firestore.collection("users").document(auth.currentUser!!.uid)
            .update("cohortsIn", FieldValue.arrayRemove(cohort.cohortUid))

        assertThat(savedCohort, `is`(notNullValue()))
        assertThat(savedCohort!!.cohortMembers.contains(auth.currentUser!!.uid), `is`(true))
        assertThat(savedCohort.numberOfMembers, `is`(1))
    }

    @Test
    fun removeThisUserFromCohort_removesTheGivenUserFromTheGivenCohort() = runBlocking {
        // Given - the given user and given cohort exist in firestore
        val fakeCohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            cohortMembers = mutableListOf("fakeuseruid"),
            numberOfMembers = 5
        )
        val fakeUser = User(
            uid = "fakeuseruid",
            cohortsIn = mutableListOf(fakeCohort.cohortUid)
        )
        // save this fake user
        firestore.collection("users").document(fakeUser.uid!!).set(fakeUser).await()
        // save this fake cohort
        firestore.collection("cohorts").document(fakeCohort.cohortUid)
            .set(fakeCohort)
            .await()

        // When - a user is removed from a cohort
        val result = repository.removeThisUserFromCohort(fakeUser, fakeCohort)

        // Then - the association of user with the cohort should be deleted
        assertThat(result.succeeded, `is`(true))
        val savedFakeUser = firestore.collection("users").document(fakeUser.uid!!)
            .get()
            .await()
            .toObject(User::class.java)!!
        val savedFakeCohort = firestore.collection("cohorts")
            .document(fakeCohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensure that the saved fake cohort and user data is deleted from firestore even if this
        // test fails
        firestore.collection("users").document(fakeUser.uid!!).delete().await()
        firestore.collection("cohorts").document(fakeCohort.cohortUid).delete().await()

        // assertions
        assertThat(savedFakeUser.cohortsIn, `is`(emptyList()))
        assertThat(savedFakeCohort.cohortMembers, `is`(emptyList()))
        assertThat(savedFakeCohort.numberOfMembers, `is`(fakeCohort.numberOfMembers - 1))
    }

}