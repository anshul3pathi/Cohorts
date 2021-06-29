package com.example.cohorts.ui.cohorts.newmember

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.cohorts.core.model.Cohort
import com.example.cohorts.fakes.FakeCohortsRepository
import com.example.cohorts.getOrAwaitValue
import com.example.cohorts.utils.generateRandomString
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [28])
class AddNewMemberViewModelTest {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: FakeCohortsRepository
    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var viewModel: AddNewMemberViewModel

    @Before
    fun init() {
        repository = FakeCohortsRepository()
        viewModel = AddNewMemberViewModel(repository, testDispatcher)
    }

    @After
    fun cleanUp() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun addNewMemberToCohort_addsNewMemberToCohort() = runBlockingTest {
        // Given - cohort that is in database and userEmail
        repository.registerCurrentUser()
        val email = "abcd@domain.com"
        val cohort = Cohort(
            cohortName = "RandomName",
            cohortDescription = "RandomDesc",
            numberOfMembers = 1,
            cohortMembers = mutableListOf(generateRandomString()),
        )
        repository.addCohort(cohort)
        // When - a new member is added
        viewModel.addNewMemberToCohort(cohort, email)
        // Then - cohort should contain the new member and addUserSuccessfully should contain true
        assertThat(viewModel.userAddedSuccessfully.getOrAwaitValue(), `is`(true))
    }

}