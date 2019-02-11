package io.github.wulkanowy.ui.modules.login.form

import io.github.wulkanowy.TestSchedulersProvider
import io.github.wulkanowy.data.db.entities.Student
import io.github.wulkanowy.data.repositories.StudentRepository
import io.github.wulkanowy.ui.modules.login.LoginErrorHandler
import io.github.wulkanowy.utils.FirebaseAnalyticsHelper
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.clearInvocations
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class LoginFormPresenterTest {

    @Mock
    lateinit var loginFormView: LoginFormView

    @Mock
    lateinit var repository: StudentRepository

    @Mock
    lateinit var errorHandler: LoginErrorHandler

    @Mock
    lateinit var analytics: FirebaseAnalyticsHelper

    private lateinit var presenter: LoginFormPresenter

    @Before
    fun initPresenter() {
        MockitoAnnotations.initMocks(this)
        clearInvocations(repository, loginFormView)
        presenter = LoginFormPresenter(TestSchedulersProvider(), errorHandler, repository, analytics, false)
        presenter.onAttachView(loginFormView)
    }

    @Test
    fun initViewTest() {
        verify(loginFormView).initView()
    }

    @Test
    fun emptyNicknameLoginTest() {
        presenter.attemptLogin("", "test123", "https://fakelog.cf")

        verify(loginFormView).setErrorNameRequired()
        verify(loginFormView, never()).setErrorPassRequired(false)
        verify(loginFormView, never()).setErrorPassInvalid(false)
    }

    @Test
    fun emptyPassLoginTest() {
        presenter.attemptLogin("@", "", "https://fakelog.cf")

        verify(loginFormView, never()).setErrorNameRequired()
        verify(loginFormView).setErrorPassRequired(true)
        verify(loginFormView, never()).setErrorPassInvalid(false)
    }

    @Test
    fun invalidPassLoginTest() {
        presenter.attemptLogin("@", "123", "https://fakelog.cf")

        verify(loginFormView, never()).setErrorNameRequired()
        verify(loginFormView, never()).setErrorPassRequired(true)
        verify(loginFormView).setErrorPassInvalid(true)
    }

    @Test
    fun loginTest() {
        val studentTest = Student(email = "test@", password = "123", endpoint = "https://fakelog.cf", loginType = "AUTO")
        doReturn(Single.just(listOf(studentTest)))
            .`when`(repository).getStudents(anyString(), anyString(), anyString(), anyString())
        presenter.attemptLogin("@", "123456", "https://fakelog.cf")

        verify(loginFormView).hideSoftKeyboard()
        verify(loginFormView).showProgress(true)
        verify(loginFormView).showProgress(false)
        verify(loginFormView).showContent(false)
        verify(loginFormView).showContent(true)
    }

    @Test
    fun loginEmptyTest() {
        doReturn(Single.just(emptyList<Student>()))
            .`when`(repository).getStudents(anyString(), anyString(), anyString(), anyString())
        presenter.attemptLogin("@", "123456", "https://fakelog.cf")

        verify(loginFormView).hideSoftKeyboard()
        verify(loginFormView).showProgress(true)
        verify(loginFormView).showProgress(false)
        verify(loginFormView).showContent(false)
        verify(loginFormView).showContent(true)
    }

    @Test
    fun loginEmptyTwiceTest() {
        doReturn(Single.just(emptyList<Student>()))
            .`when`(repository).getStudents(anyString(), anyString(), anyString(), anyString())
        presenter.attemptLogin("@", "123456", "https://fakelog.cf")
        presenter.attemptLogin("@", "123456", "https://fakelog.cf")

        verify(loginFormView, times(2)).hideSoftKeyboard()
        verify(loginFormView, times(2)).showProgress(true)
        verify(loginFormView, times(2)).showProgress(false)
        verify(loginFormView, times(2)).showContent(false)
        verify(loginFormView, times(2)).showContent(true)
    }

    @Test
    fun loginErrorTest() {
        val testException = RuntimeException("test")
        doReturn(Single.error<List<Student>>(testException))
            .`when`(repository).getStudents(anyString(), anyString(), anyString(), anyString())
        presenter.attemptLogin("@", "123456", "https://fakelog.cf")

        verify(loginFormView).hideSoftKeyboard()
        verify(loginFormView).showProgress(true)
        verify(loginFormView).showProgress(false)
        verify(loginFormView).showContent(false)
        verify(loginFormView).showContent(true)
        verify(errorHandler).dispatch(testException)
    }
}

