package me.srikavin.quiz.repository.local

import android.content.Context

import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import io.reactivex.Completable
import io.reactivex.Single
import me.srikavin.quiz.model.Quiz
import me.srikavin.quiz.network.common.model.data.ResourceId
import me.srikavin.quiz.repository.QuizRepository
import org.koin.standalone.KoinComponent
import org.koin.standalone.get
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

/**
 * Checks the local storage if quizzes are available and proxies requests to the provided online service
 * if quizzes are not available locally.
 */
class LocalQuizRepository(private val onlineService: QuizRepository.QuizService) : QuizRepository.QuizService, KoinComponent {

    private val gson: Gson = GsonBuilder().create()

    init {
        val sharedPreferences: SharedPreferences = get()
        val saved = sharedPreferences.getString("quizzes", null)

        if (saved == null) {
            val defaultListType = object : TypeToken<List<Quiz>>() {

            }.type
            val quizzes = gson.fromJson<List<Quiz>>(DEFAULT_QUIZ_JSON, defaultListType)

            quizzes.forEach { e ->
                e.isLocal = true
                e.draft = true
                localQuizzes[e.id.idString] = e
            }
        } else {

            val mapType = object : TypeToken<Map<String, Quiz>>() {

            }.type

            localQuizzes.putAll(gson.fromJson(saved, mapType))
        }
    }

    override fun getOwned(context: Context): Single<List<Quiz>> {
        return onlineService.getOwned(context).map {
            val toRet: ArrayList<Quiz> = ArrayList(appendLocalToQuizTitles(localQuizzes.values))
            toRet.addAll(it)
            return@map toRet
        }
    }

    override fun getQuizzes(): Single<List<Quiz>> {
        return onlineService.getQuizzes().onErrorReturn {
            return@onErrorReturn ArrayList(appendLocalToQuizTitles(localQuizzes.values))
        }.map {
            val toRet: ArrayList<Quiz> = ArrayList(appendLocalToQuizTitles(localQuizzes.values))
            toRet.addAll(it)
            return@map toRet
        }
    }

    override fun getQuizByID(id: String): Single<Quiz> {
        if (localQuizzes.containsKey(id)) {
            val local = localQuizzes[id]!!.copy()
            local.isLocal = true
            return Single.create { emitter -> emitter.onSuccess(local) }
        }
        return onlineService.getQuizByID(id)
    }

    private fun save() {
        val sharedPreferences: SharedPreferences = get()
        sharedPreferences.edit().putString("quizzes", gson.toJson(localQuizzes)).apply()
    }

    override fun deleteQuiz(context: Context, quiz: Quiz): Completable {
        localQuizzes.remove(quiz.id.idString)
        return onlineService.deleteQuiz(context, quiz)
    }

    private fun appendLocalToQuizTitles(quizzes: Collection<Quiz>): List<Quiz> {
        return quizzes.map { quiz ->
            quiz.copy(
                    title = "${quiz.title} (local)",
                    isLocal = true
            )
        }
    }

    override fun getQuizByID(id: String, handler: QuizRepository.QuizResponseHandler) {
        if (localQuizzes.containsKey(id)) {
            val local = localQuizzes[id]!!.copy()
            local.isLocal = true
            handler.handle(local)
            return
        }
        onlineService.getQuizByID(id, handler)
    }

    private fun processQuiz(quiz: Quiz) {
        quiz.questions
                .forEach { q ->
                    q.answers.forEach { a -> a.id = ResourceId(UUID.randomUUID().toString()) }
                }
    }

    override fun createQuiz(context: Context, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        onlineService.createQuiz(context, quiz, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                handler.handle(quiz)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                Toast.makeText(context, "Failed to save online. Saved locally.", Toast.LENGTH_SHORT).show()
                processQuiz(quiz)
                localQuizzes[quiz.id.idString] = quiz
                this@LocalQuizRepository.save()
                handler.handle(quiz)
            }
        })
    }

    override fun editQuiz(context: Context, id: String, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        //If failed to edit quiz online, it is likely that the quiz was created locally
        // and needs to be created on the server as well
        if (quiz.isLocal) {
            localQuizzes[quiz.id.idString] = quiz
            createQuiz(context, quiz, handler)
            save()
            return
        }
        onlineService.editQuiz(context, id, quiz, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                handler.handle(quiz)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                handler.handleErrors(*errors)
            }
        })
    }

    companion object {
        private val localQuizzes = ConcurrentHashMap<String, Quiz>()
        private const val DEFAULT_QUIZ_JSON = "[{\"tags\":[],\"title\":\"FBLA History\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"correct\":false,\"text\":\"1945\",\"id\":\"5c3587b2f022ea092d1ac374\"},{\"correct\":true,\"text\":\"1940\",\"id\":\"5c3587b2f022ea092d1ac373\"},{\"correct\":false,\"text\":\"1920\",\"id\":\"5c3587b2f022ea092d1ac372\"},{\"correct\":false,\"text\":\"1937\",\"id\":\"5c3587b2f022ea092d1ac371\"}],\"text\":\"When is the name \\\"Future Business Leaders of America\\\" chosen for the organization? \",\"id\":\"5c3587b2f022ea092d1ac370\"},{\"answers\":[{\"correct\":true,\"text\":\"Iowa\",\"id\":\"5c3587b2f022ea092d1ac36f\"},{\"correct\":false,\"text\":\"Delaware\",\"id\":\"5c3587b2f022ea092d1ac36e\"},{\"correct\":false,\"text\":\"Wisconsin\",\"id\":\"5c3587b2f022ea092d1ac36d\"},{\"correct\":false,\"text\":\"Tennessee\",\"id\":\"5c3587b2f022ea092d1ac36c\"}],\"text\":\"What is the first FBLA state chapter?\",\"id\":\"5c3587b2f022ea092d1ac36b\"},{\"answers\":[{\"correct\":false,\"text\":\"1956\",\"id\":\"5c3587b2f022ea092d1ac36a\"},{\"correct\":false,\"text\":\"1970\",\"id\":\"5c3587b2f022ea092d1ac369\"},{\"correct\":true,\"text\":\"1973\",\"id\":\"5c3587b2f022ea092d1ac368\"},{\"correct\":false,\"text\":\"2019\",\"id\":\"5c3587b2f022ea092d1ac367\"},{\"correct\":false,\"text\":\"2013\",\"id\":\"5c3587b2f022ea092d1ac366\"}],\"text\":\"When is Edward D. Miller appointed as full-time executive director?\",\"id\":\"5c3587b2f022ea092d1ac365\"},{\"answers\":[{\"correct\":false,\"text\":\"1982\",\"id\":\"5c3587b2f022ea092d1ac364\"},{\"correct\":true,\"text\":\"1987\",\"id\":\"5c3587b2f022ea092d1ac363\"},{\"correct\":false,\"text\":\"1977\",\"id\":\"5c3587b2f022ea092d1ac362\"},{\"correct\":false,\"text\":\"1964\",\"id\":\"5c3587b2f022ea092d1ac361\"}],\"text\":\"What year does FBLA membership exceed 200,000?\",\"id\":\"5c3587b2f022ea092d1ac360\"},{\"answers\":[{\"correct\":false,\"text\":\"1997\",\"id\":\"5c3587b2f022ea092d1ac35f\"},{\"correct\":false,\"text\":\"1936\",\"id\":\"5c3587b2f022ea092d1ac35e\"},{\"correct\":false,\"text\":\"2004\",\"id\":\"5c3587b2f022ea092d1ac35d\"},{\"correct\":false,\"text\":\"1994\",\"id\":\"5c3587b2f022ea092d1ac35c\"}],\"text\":\"When is the FBLA Middle Level Division created?\",\"id\":\"5c3587b2f022ea092d1ac35b\"}],\"description\":\"A quiz on FBLA History and major events.\\nImage credit: FBLA-PBL \",\"draft\":true,\"createdAt\":\"2019-01-09T05:33:34.270Z\",\"updatedAt\":\"2019-01-09T05:33:38.634Z\",\"coverImage\":\"http://dwhsfbla.weebly.com/uploads/1/1/2/9/1129599/2504705.jpg\",\"id\":\"5c3587aef022ea092d1ac340-local\"},{\"tags\":[],\"title\":\"FBLA NLC Dates and Locations\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"correct\":true,\"text\":\"November 1 - 2\",\"id\":\"5c358610f022ea092d1ac33f\"},{\"correct\":false,\"text\":\"November 18 - 20\",\"id\":\"5c358610f022ea092d1ac33e\"},{\"correct\":false,\"text\":\"December 1 - 2\",\"id\":\"5c358610f022ea092d1ac33d\"},{\"correct\":true,\"text\":\"November 8 - 9\",\"id\":\"5c358610f022ea092d1ac33c\"},{\"correct\":false,\"text\":\"November 21 - 23\",\"id\":\"5c358610f022ea092d1ac33b\"}],\"text\":\"When is the 2019 NFLC?\",\"id\":\"5c358610f022ea092d1ac33a\"},{\"answers\":[{\"correct\":true,\"text\":\"Washington, D.C.\",\"id\":\"5c358610f022ea092d1ac339\"},{\"correct\":false,\"text\":\"Orlando, FL\",\"id\":\"5c358610f022ea092d1ac338\"},{\"correct\":false,\"text\":\"Houston, TX\",\"id\":\"5c358610f022ea092d1ac337\"},{\"correct\":true,\"text\":\"Denver, CO\",\"id\":\"5c358610f022ea092d1ac336\"},{\"correct\":false,\"text\":\"San Antonio, TX\",\"id\":\"5c358610f022ea092d1ac335\"}],\"text\":\"Where is the 2019 NFLC?\",\"id\":\"5c358610f022ea092d1ac334\"},{\"answers\":[{\"correct\":true,\"text\":\"November 1 - 2\",\"id\":\"5c358610f022ea092d1ac333\"},{\"correct\":false,\"text\":\"November 8 - 9\",\"id\":\"5c358610f022ea092d1ac332\"},{\"correct\":false,\"text\":\"November 15 - 16\",\"id\":\"5c358610f022ea092d1ac331\"},{\"correct\":false,\"text\":\"December 2 - 3\",\"id\":\"5c358610f022ea092d1ac330\"}],\"text\":\"When is the Washington, D.C. NFLC?\",\"id\":\"5c358610f022ea092d1ac32f\"},{\"answers\":[{\"correct\":true,\"text\":\"June 29 - July 2\",\"id\":\"5c358610f022ea092d1ac32e\"},{\"correct\":false,\"text\":\"June 24 - June 27\",\"id\":\"5c358610f022ea092d1ac32d\"},{\"correct\":false,\"text\":\"July 3 - July 8\",\"id\":\"5c358610f022ea092d1ac32c\"},{\"correct\":false,\"text\":\"June 19 - June 23\",\"id\":\"5c358610f022ea092d1ac32b\"}],\"text\":\"When is the 2019 NLC for FBLA?\",\"id\":\"5c358610f022ea092d1ac32a\"},{\"answers\":[{\"correct\":true,\"text\":\"San Antonio, TX\",\"id\":\"5c358610f022ea092d1ac329\"},{\"correct\":false,\"text\":\"Houston, TX\",\"id\":\"5c358610f022ea092d1ac328\"},{\"correct\":false,\"text\":\"Orlando, FL\",\"id\":\"5c358610f022ea092d1ac327\"},{\"correct\":false,\"text\":\"Baltimore, MD\",\"id\":\"5c358610f022ea092d1ac326\"}],\"text\":\"Where is the 2019 NLC?\",\"id\":\"5c358610f022ea092d1ac325\"}],\"description\":\"A quiz on 2019 FBLA NLC and NLFC dates and locations.\\nImage credit: FBLA-PBL\",\"draft\":true,\"createdAt\":\"2019-01-09T05:26:35.599Z\",\"updatedAt\":\"2019-01-09T05:26:40.203Z\",\"coverImage\":\"https://i0.wp.com/www.fbla-pbl.org/media/Create-Lead-Inspire-Logo-Background.jpg\",\"id\":\"5c35860bf022ea092d1ac309-local\"},{\"tags\":[],\"title\":\"FBLA Dresscode\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"correct\":false,\"text\":\"All event attendees except advisers\",\"id\":\"5c3583b4f022ea092d1ac308\"},{\"correct\":false,\"text\":\"Only students\",\"id\":\"5c3583b4f022ea092d1ac307\"},{\"correct\":true,\"text\":\"All event attendees\",\"id\":\"5c3583b4f022ea092d1ac306\"}],\"text\":\"Who must follow the Dress Code?\",\"id\":\"5c3583b4f022ea092d1ac305\"},{\"answers\":[{\"correct\":true,\"text\":\"Yes\",\"id\":\"5c3583b4f022ea092d1ac304\"},{\"correct\":false,\"text\":\"No\",\"id\":\"5c3583b4f022ea092d1ac303\"}],\"text\":\"Do males need to wear a tie with a business suit?\",\"id\":\"5c3583b4f022ea092d1ac302\"},{\"answers\":[{\"correct\":true,\"text\":\"To uphold the professional image of FBLA\",\"id\":\"5c3583b4f022ea092d1ac301\"},{\"correct\":true,\"text\":\"To prepare students for the business world\",\"id\":\"5c3583b4f022ea092d1ac300\"},{\"correct\":false,\"text\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2ff\"}],\"text\":\"What is the purpose of the Dress Code?\",\"id\":\"5c3583b4f022ea092d1ac2fe\"},{\"answers\":[{\"correct\":true,\"text\":\"Denied attendence\",\"id\":\"5c3583b4f022ea092d1ac2fd\"},{\"correct\":false,\"text\":\"Allowed to continue with the conference\",\"id\":\"5c3583b4f022ea092d1ac2fc\"},{\"correct\":false,\"text\":\"Given an exception\",\"id\":\"5c3583b4f022ea092d1ac2fb\"},{\"correct\":false,\"text\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2fa\"}],\"text\":\"What is the consequence of not wearing proper attire to an FBLA conference?\",\"id\":\"5c3583b4f022ea092d1ac2f9\"},{\"answers\":[{\"correct\":true,\"text\":\"Dress Shoes\",\"id\":\"5c3583b4f022ea092d1ac2f8\"},{\"correct\":false,\"text\":\"Necktie\",\"id\":\"5c3583b4f022ea092d1ac2f7\"},{\"correct\":true,\"text\":\"Business Suit with Blouse\",\"id\":\"5c3583b4f022ea092d1ac2f6\"},{\"correct\":false,\"text\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2f5\"}],\"text\":\"What is part of the female acceptable attire?\",\"id\":\"5c3583b4f022ea092d1ac2f4\"}],\"description\":\"\",\"draft\":true,\"createdAt\":\"2019-01-09T05:13:44.101Z\",\"updatedAt\":\"2019-01-09T05:16:36.681Z\",\"coverImage\":null,\"id\":\"5c358308f022ea092d1ac2d1-local\"},{\"tags\":[],\"title\":\"FBLA Computer Problem Solving\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"correct\":true,\"text\":\"Central Processing Unit\",\"id\":\"5c349b3df456230813a80540\"},{\"correct\":false,\"text\":\"Central Printing Unit\",\"id\":\"5c349b3df456230813a8053f\"},{\"correct\":false,\"text\":\"Copy & Print User\",\"id\":\"5c349b3df456230813a8053e\"},{\"correct\":false,\"text\":\"Cancel Plus Under\",\"id\":\"5c349b3df456230813a8053d\"}],\"text\":\"What does 'CPU' stand for?\",\"id\":\"5c349b3df456230813a8053c\"},{\"answers\":[{\"correct\":false,\"text\":\"Gibson Protocol Unifier\",\"id\":\"5c349b3df456230813a8053b\"},{\"correct\":true,\"text\":\"Graphical Processing Unit\",\"id\":\"5c349b3df456230813a8053a\"},{\"correct\":false,\"text\":\"Graph Power Unit \",\"id\":\"5c349b3df456230813a80539\"},{\"correct\":false,\"text\":\"Gibson & Peter Union\",\"id\":\"5c349b3df456230813a80538\"},{\"correct\":false,\"text\":\"Gorilla Product Uno\",\"id\":\"5c349b3df456230813a80537\"}],\"text\":\"What does GPU stand for?\",\"id\":\"5c349b3df456230813a80536\"},{\"answers\":[{\"correct\":false,\"text\":\"1\",\"id\":\"5c349b3df456230813a80535\"},{\"correct\":false,\"text\":\"10\",\"id\":\"5c349b3df456230813a80534\"},{\"correct\":true,\"text\":\"8\",\"id\":\"5c349b3df456230813a80533\"},{\"correct\":false,\"text\":\"12\",\"id\":\"5c349b3df456230813a80532\"}],\"text\":\"How many bits are in a byte?\",\"id\":\"5c349b3df456230813a80531\"},{\"answers\":[{\"correct\":true,\"text\":\"Motherboard\",\"id\":\"5c349b3df456230813a80530\"},{\"correct\":false,\"text\":\"Memory\",\"id\":\"5c349b3df456230813a8052f\"},{\"correct\":false,\"text\":\"CPU\",\"id\":\"5c349b3df456230813a8052e\"},{\"correct\":false,\"text\":\"Keyboard\",\"id\":\"5c349b3df456230813a8052d\"}],\"text\":\"What does a BIOS post code 1xx indicate?\",\"id\":\"5c349b3df456230813a8052c\"},{\"answers\":[{\"correct\":true,\"text\":\"5gbps\",\"id\":\"5c349b3df456230813a8052b\"},{\"correct\":false,\"text\":\"480mbps\",\"id\":\"5c349b3df456230813a8052a\"},{\"correct\":false,\"text\":\"7gbps\",\"id\":\"5c349b3df456230813a80529\"},{\"correct\":false,\"text\":\"1gbps\",\"id\":\"5c349b3df456230813a80528\"}],\"text\":\"What is the maximum transfer rate of USB 3.0?\",\"id\":\"5c349b3df456230813a80527\"},{\"answers\":[{\"correct\":false,\"text\":\"Flash drive\",\"id\":\"5c349b3df456230813a80526\"},{\"correct\":false,\"text\":\"HDD\",\"id\":\"5c349b3df456230813a80525\"},{\"correct\":true,\"text\":\"SSD\",\"id\":\"5c349b3df456230813a80524\"},{\"correct\":false,\"text\":\"USB\",\"id\":\"5c349b3df456230813a80523\"}],\"text\":\"Which of the following use flash storage in a standard M.2. port?\",\"id\":\"5c349b3df456230813a80522\"},{\"answers\":[{\"correct\":true,\"text\":\"127\",\"id\":\"5c349b3df456230813a80521\"},{\"correct\":false,\"text\":\"128\",\"id\":\"5c349b3df456230813a80520\"},{\"correct\":false,\"text\":\"1\",\"id\":\"5c349b3df456230813a8051f\"},{\"correct\":false,\"text\":\"2\",\"id\":\"5c349b3df456230813a8051e\"},{\"correct\":false,\"text\":\"4\",\"id\":\"5c349b3df456230813a8051d\"},{\"correct\":false,\"text\":\"5\",\"id\":\"5c349b3df456230813a8051c\"},{\"correct\":false,\"text\":\"90\",\"id\":\"5c349b3df456230813a8051b\"}],\"text\":\"How many devices can be daisy chained to a single USB port?\",\"id\":\"5c349b3df456230813a8051a\"}],\"description\":\"A quiz focused on computer components and software issues, including the solutions associated with commong problems.\\nImage Credits: Wikimedia\",\"draft\":true,\"createdAt\":\"2019-01-08T05:45:26.570Z\",\"updatedAt\":\"2019-01-08T12:44:45.173Z\",\"coverImage\":\"https://upload.wikimedia.org/wikipedia/commons/6/6a/Ibm_px_xt_color.jpg\",\"id\":\"5c3438f665acc70b8488a764-local\"},{\"tags\":[],\"title\":\"FBLA Mobile App Development\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"correct\":true,\"text\":\"Google\",\"id\":\"5c349bb0f456230813a8055a\"},{\"correct\":false,\"text\":\"Apple\",\"id\":\"5c349bb0f456230813a80559\"},{\"correct\":false,\"text\":\"Samsung\",\"id\":\"5c349bb0f456230813a80558\"},{\"correct\":false,\"text\":\"LG\",\"id\":\"5c349bb0f456230813a80557\"}],\"text\":\"What company created Android?\",\"id\":\"5c349bb0f456230813a80556\"},{\"answers\":[{\"correct\":true,\"text\":\"Android Studio\",\"id\":\"5c349bb0f456230813a80555\"},{\"correct\":false,\"text\":\"Eclipse\",\"id\":\"5c349bb0f456230813a80554\"},{\"correct\":false,\"text\":\"XCode\",\"id\":\"5c349bb0f456230813a80553\"},{\"correct\":false,\"text\":\"Notepad\",\"id\":\"5c349bb0f456230813a80552\"}],\"text\":\"What is currently recommended by Google to create native Android apps?\",\"id\":\"5c349bb0f456230813a80551\"},{\"answers\":[{\"correct\":true,\"text\":\"React Native\",\"id\":\"5c349bb0f456230813a80550\"},{\"correct\":true,\"text\":\"Flutter\",\"id\":\"5c349bb0f456230813a8054f\"},{\"correct\":false,\"text\":\"CrossPlatform 3.6\",\"id\":\"5c349bb0f456230813a8054e\"},{\"correct\":true,\"text\":\"Ionic\",\"id\":\"5c349bb0f456230813a8054d\"}],\"text\":\"Which of the following are frameworks that are capable of developing cross-platform apps?\",\"id\":\"5c349bb0f456230813a8054c\"},{\"answers\":[{\"correct\":true,\"text\":\"XML\",\"id\":\"5c349bb0f456230813a8054b\"},{\"correct\":false,\"text\":\"JSON\",\"id\":\"5c349bb0f456230813a8054a\"},{\"correct\":false,\"text\":\"YML\",\"id\":\"5c349bb0f456230813a80549\"},{\"correct\":false,\"text\":\"SQL\",\"id\":\"5c349bb0f456230813a80548\"}],\"text\":\"How are layouts generally defined in Android apps?\",\"id\":\"5c349bb0f456230813a80547\"},{\"answers\":[{\"correct\":true,\"text\":\"Java\",\"id\":\"5c349bb0f456230813a80546\"},{\"correct\":true,\"text\":\"Kotlin\",\"id\":\"5c349bb0f456230813a80545\"},{\"correct\":false,\"text\":\"Swift\",\"id\":\"5c349bb0f456230813a80544\"},{\"correct\":false,\"text\":\"C#\",\"id\":\"5c349bb0f456230813a80543\"},{\"correct\":false,\"text\":\"APL\",\"id\":\"5c349bb0f456230813a80542\"}],\"text\":\"What language has Google officially supported for Android Development?\",\"id\":\"5c349bb0f456230813a80541\"}],\"description\":\"A quiz focused on the development of iOS and Android apps.\\nImage Credit: Android.com\",\"draft\":true,\"createdAt\":\"2019-01-08T05:40:57.946Z\",\"updatedAt\":\"2019-01-08T12:46:40.542Z\",\"coverImage\":\"https://www.android.com/static/2016/img/devices/phones/htc-u11/htc-u11_1x.png\",\"id\":\"5c3437e965acc70b8488a702-local\"}]"
    }
}