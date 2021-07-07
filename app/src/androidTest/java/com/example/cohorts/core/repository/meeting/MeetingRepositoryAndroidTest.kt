package com.example.cohorts.core.repository.meeting

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.MediumTest
import com.example.cohorts.core.Result
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.core.succeeded
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import javax.inject.Inject


@ExperimentalCoroutinesApi
@HiltAndroidTest
@MediumTest
class MeetingRepositoryAndroidTest {

    private val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var repository: MeetingRepository
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    @get:Rule
    var rule = RuleChain.outerRule(hiltRule).around(instantTaskExecutorRule)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun addCurrentUserToOngoingMeeting() = runBlocking {
        // Given - cohort exists in firestore
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()

        // When - current user is added to meeting of this cohort
        repository.addCurrentUserToOngoingMeeting(cohort.cohortUid)

        // Then - current user uid should be added to membersInMeeting array of given cohort
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        // retrieved cohort is same as saved
        assertThat(savedCohort.cohortUid, `is`(cohort.cohortUid))
        // current user is in meeting
        assertThat(auth.currentUser!!.uid in savedCohort.membersInMeeting, `is`(true))
    }

    @Test
    fun startNewMeeting_startsNewMeetingIfNoMeetingIsGoingOn() = runBlocking {
        // Given - cohort exists in firestore
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()

        // When - a new meeting is started
        val result = repository.startNewMeeting(cohort.cohortUid)

        // Then - a new meeting of this cohort should be started
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        assertThat(result.succeeded, `is`(true)) // operation returned success

        // current user is in meeting
        assertThat(auth.currentUser!!.uid in savedCohort.membersInMeeting, `is`(true))

        assertThat(savedCohort.isCallOngoing, `is`(true))
    }

    @Test
    fun startNewMeeting_returnsErrorWhenAMeetingIsGoingOn() = runBlocking {
        // Given - cohort exists in firestore and a meeting is going on in this cohort
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()

        // When - a new meeting is started
        val result = repository.startNewMeeting(cohort.cohortUid)

        // Then - it should return an error and no changes should be made in the given cohort
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        assertThat(result.succeeded, `is`(false)) // operation returned an error

        val exception = (result as Result.Error).exception
        assertThat(exception.message, `is`("Meeting already going on!"))

        assertThat(savedCohort, `is`(cohort)) // no changes were made to the cohort in firestore
    }

    @Test
    fun leaveOngoingMeeting_removesUserFromMeeting_closesTheMeetingIfNoOtherParticipants()
    = runBlocking {
        // Given - cohort exists in firestore and current user is in the meeting of this cohort
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true,
            membersInMeeting = mutableListOf(auth.currentUser!!.uid)
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()

        // When - the user tries to leave the meeting
        val result = repository.leaveOngoingMeeting()

        // Then - the user is removed from meeting and the meeting is ended if no other
        // participants are in the meeting
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        assertThat(result.succeeded, `is`(true)) // operation returned success

        // current user was removed from the ongoing meeting
        assertThat(auth.currentUser!!.uid !in savedCohort.membersInMeeting, `is`(true))

        assertThat(savedCohort.isCallOngoing, `is`(false)) // meeting has ended
    }

    @Test
    fun leaveOngoingMeeting_removedUserFromMeeting_doesntEndTheCallIfThereAreParticipants()
    = runBlocking {
        // Given - cohort exists in firestore and current user is in the meeting of this cohort
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true,
            // other participants are also in meeting
            membersInMeeting = mutableListOf(auth.currentUser!!.uid, "user2uid", "user3uid")
        )
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()

        // When - the user tries to leave the meeting
        val result = repository.leaveOngoingMeeting()

        // Then - the user is removed from meeting and the meeting is not ended as other
        // participants are in the meeting
        val savedCohort = firestore.collection("cohorts").document(cohort.cohortUid)
            .get()
            .await()
            .toObject(Cohort::class.java)!!

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        firestore.collection("cohorts").document(cohort.cohortUid).delete().await()

        assertThat(result.succeeded, `is`(true)) // the operation returned success

        // the current user was removed from the meeting
        assertThat(auth.currentUser!!.uid !in savedCohort.membersInMeeting, `is`(true))

        assertThat(savedCohort.isCallOngoing, `is`(true)) // the meeting is still going on
    }

}