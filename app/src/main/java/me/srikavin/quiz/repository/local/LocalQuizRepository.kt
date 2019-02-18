package me.srikavin.quiz.repository.local

import android.content.Context
import android.content.SharedPreferences
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

internal class LocalQuizRepository(private val onlineService: QuizRepository.QuizService) : QuizRepository.QuizService, KoinComponent {

    override fun deleteQuiz(context: Context, quiz: Quiz): Completable {
        localQuizzes.remove(quiz.id.idString)
        return onlineService.deleteQuiz(context, quiz)
    }

    override fun getOwned(context: Context): Single<List<Quiz>> {
        return onlineService.getOwned(context).map {
            val toRet: ArrayList<Quiz> = ArrayList(appendLocalToQuizTitles(localQuizzes.values))
            toRet.addAll(it)
            return@map toRet
        }
    }

    override fun getQuizzes(): Single<List<Quiz>> {
        return onlineService.getQuizzes().map {
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

    fun save() {
        val sharedPreferences: SharedPreferences = get()
        sharedPreferences.edit().putString("quizzes", gson.toJson(localQuizzes)).apply()
    }

    private fun appendLocalToQuizTitles(quizzes: Collection<Quiz>): List<Quiz> {
        return quizzes.map { quiz ->
            quiz.copy(
                    title = "${quiz.title} (local)",
                    isLocal = true
            )
        }
    }

    override fun getQuizzes(handler: QuizRepository.QuizResponseHandler) {
        onlineService.getQuizzes(object : QuizRepository.QuizResponseHandler() {
            override fun handleMultiple(quizzes: List<Quiz>) {
                // Necessary in case that the list returned by the online service is immutable
                // Append (local) to all local quiz titles
                val toRet = ArrayList(
                        appendLocalToQuizTitles(localQuizzes.values))
                toRet.addAll(quizzes)
                handler.handleMultiple(toRet)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                handler.handleMultiple(ArrayList(appendLocalToQuizTitles(localQuizzes.values)))
            }
        })
    }

    override fun getOwned(context: Context, handler: QuizRepository.QuizResponseHandler) {
        onlineService.getOwned(context, object : QuizRepository.QuizResponseHandler() {
            override fun handleMultiple(quizzes: List<Quiz>) {
                //Necessary in case that the list returned by the online service is immutable
                val toRet = ArrayList(appendLocalToQuizTitles(localQuizzes.values))
                toRet.addAll(quizzes)
                handler.handleMultiple(toRet)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                handler.handleMultiple(ArrayList(appendLocalToQuizTitles(localQuizzes.values)))
            }
        })
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
//                Toast.makeText(this@LocalQuizRepository.context, "Failed to save online. Saved locally.", Toast.LENGTH_SHORT).show()
                processQuiz(quiz)
                localQuizzes[quiz.id.idString] = quiz
                this@LocalQuizRepository.save()
                handler.handle(quiz)
            }
        })
    }

    override fun editQuiz(context: Context, id: String, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        onlineService.editQuiz(context, id, quiz, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                handler.handle(quiz)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                //If failed to edit quiz online, it is likely that the quiz was created locally
                // and needs to be created on the server as well
                if (localQuizzes.containsKey(id)) {
//                    createQuiz(this@LocalQuizRepository.context, quiz, handler)
                    return
                }

                handler.handleErrors(*errors)
            }
        })
    }

    override fun deleteQuiz(context: Context, quiz: Quiz, handler: QuizRepository.QuizResponseHandler) {
        //Delete locally as well
        localQuizzes.remove(quiz.id.idString)
        onlineService.deleteQuiz(context, quiz, object : QuizRepository.QuizResponseHandler() {
            override fun handle(quiz: Quiz?) {
                super.handle(quiz)
                handler.handle(quiz)
            }

            override fun handleErrors(vararg errors: QuizRepository.ErrorCodes) {
                super.handleErrors(*errors)
                handler.handle(null)
            }
        })
        this@LocalQuizRepository.save()
    }

    companion object {
        private val localQuizzes = ConcurrentHashMap<String, Quiz>()
        private const val DEFAULT_QUIZ_JSON = "[{\"tags\":[],\"title\":\"FBLA History\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"isCorrect\":false,\"contents\":\"1945\",\"id\":\"5c3587b2f022ea092d1ac374\"},{\"isCorrect\":true,\"contents\":\"1940\",\"id\":\"5c3587b2f022ea092d1ac373\"},{\"isCorrect\":false,\"contents\":\"1920\",\"id\":\"5c3587b2f022ea092d1ac372\"},{\"isCorrect\":false,\"contents\":\"1937\",\"id\":\"5c3587b2f022ea092d1ac371\"}],\"contents\":\"When is the name \\\"Future Business Leaders of America\\\" chosen for the organization? \",\"id\":\"5c3587b2f022ea092d1ac370\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Iowa\",\"id\":\"5c3587b2f022ea092d1ac36f\"},{\"isCorrect\":false,\"contents\":\"Delaware\",\"id\":\"5c3587b2f022ea092d1ac36e\"},{\"isCorrect\":false,\"contents\":\"Wisconsin\",\"id\":\"5c3587b2f022ea092d1ac36d\"},{\"isCorrect\":false,\"contents\":\"Tennessee\",\"id\":\"5c3587b2f022ea092d1ac36c\"}],\"contents\":\"What is the first FBLA state chapter?\",\"id\":\"5c3587b2f022ea092d1ac36b\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"1956\",\"id\":\"5c3587b2f022ea092d1ac36a\"},{\"isCorrect\":false,\"contents\":\"1970\",\"id\":\"5c3587b2f022ea092d1ac369\"},{\"isCorrect\":true,\"contents\":\"1973\",\"id\":\"5c3587b2f022ea092d1ac368\"},{\"isCorrect\":false,\"contents\":\"2019\",\"id\":\"5c3587b2f022ea092d1ac367\"},{\"isCorrect\":false,\"contents\":\"2013\",\"id\":\"5c3587b2f022ea092d1ac366\"}],\"contents\":\"When is Edward D. Miller appointed as full-time executive director?\",\"id\":\"5c3587b2f022ea092d1ac365\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"1982\",\"id\":\"5c3587b2f022ea092d1ac364\"},{\"isCorrect\":true,\"contents\":\"1987\",\"id\":\"5c3587b2f022ea092d1ac363\"},{\"isCorrect\":false,\"contents\":\"1977\",\"id\":\"5c3587b2f022ea092d1ac362\"},{\"isCorrect\":false,\"contents\":\"1964\",\"id\":\"5c3587b2f022ea092d1ac361\"}],\"contents\":\"What year does FBLA membership exceed 200,000?\",\"id\":\"5c3587b2f022ea092d1ac360\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"1997\",\"id\":\"5c3587b2f022ea092d1ac35f\"},{\"isCorrect\":false,\"contents\":\"1936\",\"id\":\"5c3587b2f022ea092d1ac35e\"},{\"isCorrect\":false,\"contents\":\"2004\",\"id\":\"5c3587b2f022ea092d1ac35d\"},{\"isCorrect\":false,\"contents\":\"1994\",\"id\":\"5c3587b2f022ea092d1ac35c\"}],\"contents\":\"When is the FBLA Middle Level Division created?\",\"id\":\"5c3587b2f022ea092d1ac35b\"}],\"description\":\"A quiz on FBLA History and major events.\\nImage credit: FBLA-PBL \",\"draft\":true,\"createdAt\":\"2019-01-09T05:33:34.270Z\",\"updatedAt\":\"2019-01-09T05:33:38.634Z\",\"coverImage\":\"http://dwhsfbla.weebly.com/uploads/1/1/2/9/1129599/2504705.jpg\",\"id\":\"5c3587aef022ea092d1ac340-local\"},{\"tags\":[],\"title\":\"FBLA NLC Dates and Locations\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"isCorrect\":true,\"contents\":\"November 1 - 2\",\"id\":\"5c358610f022ea092d1ac33f\"},{\"isCorrect\":false,\"contents\":\"November 18 - 20\",\"id\":\"5c358610f022ea092d1ac33e\"},{\"isCorrect\":false,\"contents\":\"December 1 - 2\",\"id\":\"5c358610f022ea092d1ac33d\"},{\"isCorrect\":true,\"contents\":\"November 8 - 9\",\"id\":\"5c358610f022ea092d1ac33c\"},{\"isCorrect\":false,\"contents\":\"November 21 - 23\",\"id\":\"5c358610f022ea092d1ac33b\"}],\"contents\":\"When is the 2019 NFLC?\",\"id\":\"5c358610f022ea092d1ac33a\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Washington, D.C.\",\"id\":\"5c358610f022ea092d1ac339\"},{\"isCorrect\":false,\"contents\":\"Orlando, FL\",\"id\":\"5c358610f022ea092d1ac338\"},{\"isCorrect\":false,\"contents\":\"Houston, TX\",\"id\":\"5c358610f022ea092d1ac337\"},{\"isCorrect\":true,\"contents\":\"Denver, CO\",\"id\":\"5c358610f022ea092d1ac336\"},{\"isCorrect\":false,\"contents\":\"San Antonio, TX\",\"id\":\"5c358610f022ea092d1ac335\"}],\"contents\":\"Where is the 2019 NFLC?\",\"id\":\"5c358610f022ea092d1ac334\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"November 1 - 2\",\"id\":\"5c358610f022ea092d1ac333\"},{\"isCorrect\":false,\"contents\":\"November 8 - 9\",\"id\":\"5c358610f022ea092d1ac332\"},{\"isCorrect\":false,\"contents\":\"November 15 - 16\",\"id\":\"5c358610f022ea092d1ac331\"},{\"isCorrect\":false,\"contents\":\"December 2 - 3\",\"id\":\"5c358610f022ea092d1ac330\"}],\"contents\":\"When is the Washington, D.C. NFLC?\",\"id\":\"5c358610f022ea092d1ac32f\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"June 29 - July 2\",\"id\":\"5c358610f022ea092d1ac32e\"},{\"isCorrect\":false,\"contents\":\"June 24 - June 27\",\"id\":\"5c358610f022ea092d1ac32d\"},{\"isCorrect\":false,\"contents\":\"July 3 - July 8\",\"id\":\"5c358610f022ea092d1ac32c\"},{\"isCorrect\":false,\"contents\":\"June 19 - June 23\",\"id\":\"5c358610f022ea092d1ac32b\"}],\"contents\":\"When is the 2019 NLC for FBLA?\",\"id\":\"5c358610f022ea092d1ac32a\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"San Antonio, TX\",\"id\":\"5c358610f022ea092d1ac329\"},{\"isCorrect\":false,\"contents\":\"Houston, TX\",\"id\":\"5c358610f022ea092d1ac328\"},{\"isCorrect\":false,\"contents\":\"Orlando, FL\",\"id\":\"5c358610f022ea092d1ac327\"},{\"isCorrect\":false,\"contents\":\"Baltimore, MD\",\"id\":\"5c358610f022ea092d1ac326\"}],\"contents\":\"Where is the 2019 NLC?\",\"id\":\"5c358610f022ea092d1ac325\"}],\"description\":\"A quiz on 2019 FBLA NLC and NLFC dates and locations.\\nImage credit: FBLA-PBL\",\"draft\":true,\"createdAt\":\"2019-01-09T05:26:35.599Z\",\"updatedAt\":\"2019-01-09T05:26:40.203Z\",\"coverImage\":\"https://i0.wp.com/www.fbla-pbl.org/media/Create-Lead-Inspire-Logo-Background.jpg\",\"id\":\"5c35860bf022ea092d1ac309-local\"},{\"tags\":[],\"title\":\"FBLA Dresscode\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"isCorrect\":false,\"contents\":\"All event attendees except advisers\",\"id\":\"5c3583b4f022ea092d1ac308\"},{\"isCorrect\":false,\"contents\":\"Only students\",\"id\":\"5c3583b4f022ea092d1ac307\"},{\"isCorrect\":true,\"contents\":\"All event attendees\",\"id\":\"5c3583b4f022ea092d1ac306\"}],\"contents\":\"Who must follow the Dress Code?\",\"id\":\"5c3583b4f022ea092d1ac305\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Yes\",\"id\":\"5c3583b4f022ea092d1ac304\"},{\"isCorrect\":false,\"contents\":\"No\",\"id\":\"5c3583b4f022ea092d1ac303\"}],\"contents\":\"Do males need to wear a tie with a business suit?\",\"id\":\"5c3583b4f022ea092d1ac302\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"To uphold the professional image of FBLA\",\"id\":\"5c3583b4f022ea092d1ac301\"},{\"isCorrect\":true,\"contents\":\"To prepare students for the business world\",\"id\":\"5c3583b4f022ea092d1ac300\"},{\"isCorrect\":false,\"contents\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2ff\"}],\"contents\":\"What is the purpose of the Dress Code?\",\"id\":\"5c3583b4f022ea092d1ac2fe\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Denied attendence\",\"id\":\"5c3583b4f022ea092d1ac2fd\"},{\"isCorrect\":false,\"contents\":\"Allowed to continue with the conference\",\"id\":\"5c3583b4f022ea092d1ac2fc\"},{\"isCorrect\":false,\"contents\":\"Given an exception\",\"id\":\"5c3583b4f022ea092d1ac2fb\"},{\"isCorrect\":false,\"contents\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2fa\"}],\"contents\":\"What is the consequence of not wearing proper attire to an FBLA conference?\",\"id\":\"5c3583b4f022ea092d1ac2f9\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Dress Shoes\",\"id\":\"5c3583b4f022ea092d1ac2f8\"},{\"isCorrect\":false,\"contents\":\"Necktie\",\"id\":\"5c3583b4f022ea092d1ac2f7\"},{\"isCorrect\":true,\"contents\":\"Business Suit with Blouse\",\"id\":\"5c3583b4f022ea092d1ac2f6\"},{\"isCorrect\":false,\"contents\":\"None of the Other Answers\",\"id\":\"5c3583b4f022ea092d1ac2f5\"}],\"contents\":\"What is part of the female acceptable attire?\",\"id\":\"5c3583b4f022ea092d1ac2f4\"}],\"description\":\"\",\"draft\":true,\"createdAt\":\"2019-01-09T05:13:44.101Z\",\"updatedAt\":\"2019-01-09T05:16:36.681Z\",\"coverImage\":null,\"id\":\"5c358308f022ea092d1ac2d1-local\"},{\"tags\":[],\"title\":\"FBLA Computer Problem Solving\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"isCorrect\":true,\"contents\":\"Central Processing Unit\",\"id\":\"5c349b3df456230813a80540\"},{\"isCorrect\":false,\"contents\":\"Central Printing Unit\",\"id\":\"5c349b3df456230813a8053f\"},{\"isCorrect\":false,\"contents\":\"Copy & Print User\",\"id\":\"5c349b3df456230813a8053e\"},{\"isCorrect\":false,\"contents\":\"Cancel Plus Under\",\"id\":\"5c349b3df456230813a8053d\"}],\"contents\":\"What does 'CPU' stand for?\",\"id\":\"5c349b3df456230813a8053c\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"Gibson Protocol Unifier\",\"id\":\"5c349b3df456230813a8053b\"},{\"isCorrect\":true,\"contents\":\"Graphical Processing Unit\",\"id\":\"5c349b3df456230813a8053a\"},{\"isCorrect\":false,\"contents\":\"Graph Power Unit \",\"id\":\"5c349b3df456230813a80539\"},{\"isCorrect\":false,\"contents\":\"Gibson & Peter Union\",\"id\":\"5c349b3df456230813a80538\"},{\"isCorrect\":false,\"contents\":\"Gorilla Product Uno\",\"id\":\"5c349b3df456230813a80537\"}],\"contents\":\"What does GPU stand for?\",\"id\":\"5c349b3df456230813a80536\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"1\",\"id\":\"5c349b3df456230813a80535\"},{\"isCorrect\":false,\"contents\":\"10\",\"id\":\"5c349b3df456230813a80534\"},{\"isCorrect\":true,\"contents\":\"8\",\"id\":\"5c349b3df456230813a80533\"},{\"isCorrect\":false,\"contents\":\"12\",\"id\":\"5c349b3df456230813a80532\"}],\"contents\":\"How many bits are in a byte?\",\"id\":\"5c349b3df456230813a80531\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Motherboard\",\"id\":\"5c349b3df456230813a80530\"},{\"isCorrect\":false,\"contents\":\"Memory\",\"id\":\"5c349b3df456230813a8052f\"},{\"isCorrect\":false,\"contents\":\"CPU\",\"id\":\"5c349b3df456230813a8052e\"},{\"isCorrect\":false,\"contents\":\"Keyboard\",\"id\":\"5c349b3df456230813a8052d\"}],\"contents\":\"What does a BIOS post code 1xx indicate?\",\"id\":\"5c349b3df456230813a8052c\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"5gbps\",\"id\":\"5c349b3df456230813a8052b\"},{\"isCorrect\":false,\"contents\":\"480mbps\",\"id\":\"5c349b3df456230813a8052a\"},{\"isCorrect\":false,\"contents\":\"7gbps\",\"id\":\"5c349b3df456230813a80529\"},{\"isCorrect\":false,\"contents\":\"1gbps\",\"id\":\"5c349b3df456230813a80528\"}],\"contents\":\"What is the maximum transfer rate of USB 3.0?\",\"id\":\"5c349b3df456230813a80527\"},{\"answers\":[{\"isCorrect\":false,\"contents\":\"Flash drive\",\"id\":\"5c349b3df456230813a80526\"},{\"isCorrect\":false,\"contents\":\"HDD\",\"id\":\"5c349b3df456230813a80525\"},{\"isCorrect\":true,\"contents\":\"SSD\",\"id\":\"5c349b3df456230813a80524\"},{\"isCorrect\":false,\"contents\":\"USB\",\"id\":\"5c349b3df456230813a80523\"}],\"contents\":\"Which of the following use flash storage in a standard M.2. port?\",\"id\":\"5c349b3df456230813a80522\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"127\",\"id\":\"5c349b3df456230813a80521\"},{\"isCorrect\":false,\"contents\":\"128\",\"id\":\"5c349b3df456230813a80520\"},{\"isCorrect\":false,\"contents\":\"1\",\"id\":\"5c349b3df456230813a8051f\"},{\"isCorrect\":false,\"contents\":\"2\",\"id\":\"5c349b3df456230813a8051e\"},{\"isCorrect\":false,\"contents\":\"4\",\"id\":\"5c349b3df456230813a8051d\"},{\"isCorrect\":false,\"contents\":\"5\",\"id\":\"5c349b3df456230813a8051c\"},{\"isCorrect\":false,\"contents\":\"90\",\"id\":\"5c349b3df456230813a8051b\"}],\"contents\":\"How many devices can be daisy chained to a single USB port?\",\"id\":\"5c349b3df456230813a8051a\"}],\"description\":\"A quiz focused on computer components and software issues, including the solutions associated with commong problems.\\nImage Credits: Wikimedia\",\"draft\":true,\"createdAt\":\"2019-01-08T05:45:26.570Z\",\"updatedAt\":\"2019-01-08T12:44:45.173Z\",\"coverImage\":\"https://upload.wikimedia.org/wikipedia/commons/6/6a/Ibm_px_xt_color.jpg\",\"id\":\"5c3438f665acc70b8488a764-local\"},{\"tags\":[],\"title\":\"FBLA Mobile App Development\",\"author\":{\"username\":\"username\",\"id\":\"5c3405f265acc70b8488a6f4\"},\"questions\":[{\"answers\":[{\"isCorrect\":true,\"contents\":\"Google\",\"id\":\"5c349bb0f456230813a8055a\"},{\"isCorrect\":false,\"contents\":\"Apple\",\"id\":\"5c349bb0f456230813a80559\"},{\"isCorrect\":false,\"contents\":\"Samsung\",\"id\":\"5c349bb0f456230813a80558\"},{\"isCorrect\":false,\"contents\":\"LG\",\"id\":\"5c349bb0f456230813a80557\"}],\"contents\":\"What company created Android?\",\"id\":\"5c349bb0f456230813a80556\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Android Studio\",\"id\":\"5c349bb0f456230813a80555\"},{\"isCorrect\":false,\"contents\":\"Eclipse\",\"id\":\"5c349bb0f456230813a80554\"},{\"isCorrect\":false,\"contents\":\"XCode\",\"id\":\"5c349bb0f456230813a80553\"},{\"isCorrect\":false,\"contents\":\"Notepad\",\"id\":\"5c349bb0f456230813a80552\"}],\"contents\":\"What is currently recommended by Google to create native Android apps?\",\"id\":\"5c349bb0f456230813a80551\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"React Native\",\"id\":\"5c349bb0f456230813a80550\"},{\"isCorrect\":true,\"contents\":\"Flutter\",\"id\":\"5c349bb0f456230813a8054f\"},{\"isCorrect\":false,\"contents\":\"CrossPlatform 3.6\",\"id\":\"5c349bb0f456230813a8054e\"},{\"isCorrect\":true,\"contents\":\"Ionic\",\"id\":\"5c349bb0f456230813a8054d\"}],\"contents\":\"Which of the following are frameworks that are capable of developing cross-platform apps?\",\"id\":\"5c349bb0f456230813a8054c\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"XML\",\"id\":\"5c349bb0f456230813a8054b\"},{\"isCorrect\":false,\"contents\":\"JSON\",\"id\":\"5c349bb0f456230813a8054a\"},{\"isCorrect\":false,\"contents\":\"YML\",\"id\":\"5c349bb0f456230813a80549\"},{\"isCorrect\":false,\"contents\":\"SQL\",\"id\":\"5c349bb0f456230813a80548\"}],\"contents\":\"How are layouts generally defined in Android apps?\",\"id\":\"5c349bb0f456230813a80547\"},{\"answers\":[{\"isCorrect\":true,\"contents\":\"Java\",\"id\":\"5c349bb0f456230813a80546\"},{\"isCorrect\":true,\"contents\":\"Kotlin\",\"id\":\"5c349bb0f456230813a80545\"},{\"isCorrect\":false,\"contents\":\"Swift\",\"id\":\"5c349bb0f456230813a80544\"},{\"isCorrect\":false,\"contents\":\"C#\",\"id\":\"5c349bb0f456230813a80543\"},{\"isCorrect\":false,\"contents\":\"APL\",\"id\":\"5c349bb0f456230813a80542\"}],\"contents\":\"What language has Google officially supported for Android Development?\",\"id\":\"5c349bb0f456230813a80541\"}],\"description\":\"A quiz focused on the development of iOS and Android apps.\\nImage Credit: Android.com\",\"draft\":true,\"createdAt\":\"2019-01-08T05:40:57.946Z\",\"updatedAt\":\"2019-01-08T12:46:40.542Z\",\"coverImage\":\"https://www.android.com/static/2016/img/devices/phones/htc-u11/htc-u11_1x.png\",\"id\":\"5c3437e965acc70b8488a702-local\"}]"
    }
}