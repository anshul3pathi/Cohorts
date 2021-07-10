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
        addNewCohortToFirestore(cohort)

        // When - current user is added to meeting of this cohort
        repository.addCurrentUserToOngoingMeeting(cohort.cohortUid)

        // Then - current user uid should be added to membersInMeeting array of given cohort
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

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
        addNewCohortToFirestore(cohort)

        // When - a new meeting is started
        val result = repository.startNewMeeting(cohort.cohortUid)

        // Then - a new meeting of this cohort should be started
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

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
        addNewCohortToFirestore(cohort)

        // When - a new meeting is started
        val result = repository.startNewMeeting(cohort.cohortUid)

        // Then - it should return an error and no changes should be made in the given cohort
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

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
        addNewCohortToFirestore(cohort)

        // When - the user tries to leave the meeting
        val result = repository.leaveOngoingMeeting()

        // Then - the user is removed from meeting and the meeting is ended if no other
        // participants are in the meeting
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

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
        addNewCohortToFirestore(cohort)

        // When - the user tries to leave the meeting
        val result = repository.leaveOngoingMeeting()

        // Then - the user is removed from meeting and the meeting is not ended as other
        // participants are in the meeting
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort gets deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

        assertThat(result.succeeded, `is`(true)) // the operation returned success
        // the current user was removed from the meeting
        assertThat(auth.currentUser!!.uid !in savedCohort.membersInMeeting, `is`(true))
        assertThat(savedCohort.isCallOngoing, `is`(true)) // the meeting is still going on
    }

    @Test fun onDestroy_userIsInMultipleMeetings() = runBlocking {
        // Given - the current user is in meetings of multiple cohorts
        val cohort1 = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true,
            // more than one member in meeting
            membersInMeeting = mutableListOf(auth.currentUser!!.uid, "adsfadfsasdf")
        )
        val cohort2 = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true,
            membersInMeeting = mutableListOf(auth.currentUser!!.uid)
        )
        addNewCohortToFirestore(cohort1)
        addNewCohortToFirestore(cohort2)

        // When - onDestroy is called
        repository.onDestroy()

        // Then - the current user should be removed from all the meetings
        val savedCohort1 = getCohortFromFirestore(cohort1.cohortUid)
        val savedCohort2 = getCohortFromFirestore(cohort2.cohortUid)

        // ensuring that the cohorts are deleted from firestore even if the test fails
        deleteCohortFromFirestore(cohort1.cohortUid)
        deleteCohortFromFirestore(cohort2.cohortUid)

        // current user is removed from the meeting
        assertThat(auth.currentUser!!.uid !in savedCohort1.membersInMeeting, `is`(true))
        // but the call is still going on as there were more members in meeting
        assertThat(savedCohort1.isCallOngoing, `is`(true))

        assertThat(auth.currentUser!!.uid !in savedCohort2.membersInMeeting, `is`(true))
        // as the user was the only one in meeting, therefore the call is ended
        assertThat(savedCohort2.isCallOngoing, `is`(false))
    }

    @Test
    fun onDestroy_userIsInSingleMeeting() = runBlocking {
        // Given - user is in meeting of a cohort
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf("fake user uid"),
            isCallOngoing = true,
            // more than one member in meeting
            membersInMeeting = mutableListOf(auth.currentUser!!.uid, "adsfadfsasdf")
        )
        addNewCohortToFirestore(cohort)

        // When - onDestroy is called
        repository.onDestroy()

        // Then - the user should be removed from all the meetings they are in
        val savedCohort = getCohortFromFirestore(cohort.cohortUid)

        // ensuring that the saved cohort is deleted from firestore even if this test fails
        deleteCohortFromFirestore(cohort.cohortUid)

        // user was removed from the meeting
        assertThat(auth.currentUser!!.uid !in savedCohort.membersInMeeting, `is`(true))
        // but the meeting is still ongoing
        assertThat(savedCohort.isCallOngoing, `is`(true))
    }

    private suspend fun addNewCohortToFirestore(cohort: Cohort) {
        firestore.collection("cohorts").document(cohort.cohortUid).set(cohort).await()
    }

    private suspend fun deleteCohortFromFirestore(cohortUid: String) {
        firestore.collection("cohorts").document(cohortUid).delete().await()
    }

    private suspend fun getCohortFromFirestore(cohortUid: String): Cohort {
        return firestore.collection("cohorts").document(cohortUid)
            .get().await().toObject(Cohort::class.java)!!
    }

}