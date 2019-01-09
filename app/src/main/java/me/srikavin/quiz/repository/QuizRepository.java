package me.srikavin.quiz.repository;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;
import me.srikavin.quiz.model.Quiz;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

import static me.srikavin.quiz.MainActivity.TAG;

public class QuizRepository {
    private Context context;
    private QuizService quizRepository;

    public QuizRepository(Context context) {
        this.context = context;
        quizRepository = new LocalQuizRepository(context, new InternetQuizRepository());
    }

    public void getQuizByID(String id, QuizResponseHandler handler) {
        quizRepository.getQuizByID(id, handler);
    }

    public void getQuizzes(QuizResponseHandler handler) {
        quizRepository.getQuizzes(handler);
    }

    public void getOwned(Context context, QuizResponseHandler handler) {
        quizRepository.getOwned(context, handler);
    }

    public void createQuiz(Quiz quiz, QuizResponseHandler handler) {
        quizRepository.createQuiz(context, quiz, handler);
    }

    public void editQuiz(String id, Quiz quiz, QuizResponseHandler handler) {
        quizRepository.editQuiz(context, id, quiz, handler);
    }

    public void deleteQuiz(Quiz quiz, QuizResponseHandler handler) {
        quizRepository.deleteQuiz(context, quiz, handler);
    }

    public enum ErrorCodes {
        UNKNOWN_ERROR(0),
        NETWORK_ERROR(5),
        SERVER_ERROR(6);

        private int code;

        ErrorCodes(int code) {
            this.code = code;
        }

        static ErrorCodes fromCode(int errorCode) {
            for (ErrorCodes e : values()) {
                if (errorCode == e.code) {
                    return e;
                }
            }
            return UNKNOWN_ERROR;
        }
    }

    interface QuizService {
        void getQuizzes(QuizResponseHandler handler);

        void getOwned(Context context, QuizResponseHandler handler);

        void getQuizByID(String id, QuizResponseHandler handler);

        void createQuiz(Context context, Quiz quiz, QuizResponseHandler handler);

        void editQuiz(Context context, String id, Quiz quiz, QuizResponseHandler handler);

        void deleteQuiz(Context context, Quiz quiz, QuizResponseHandler handler);
    }

    public abstract static class QuizResponseHandler extends Repository.ResponseHandler<ErrorCodes, Quiz> {
        public void handle(Quiz quiz) {
            //By default, do nothing
        }

        public void handleMultiple(List<Quiz> quizzes) {
            //By default, do nothing
        }

        @Override
        public void handleErrors(@NonNull ErrorCodes... errors) {
            //By default, print error codes
            for (QuizRepository.ErrorCodes e : errors) {
                Log.w(TAG, "Ignored error code: " + e.name());
            }
        }
    }

    static class InternetQuizRepository extends InternetRepository<Quiz, ErrorCodes, Repository.ResponseHandler<ErrorCodes, Quiz>> implements QuizService {

        private final InternetQuizService quizService;

        @Override
        protected ErrorCodes mapIntegerErrorCode(int error) {
            return ErrorCodes.fromCode(error);
        }

        @Override
        protected void forwardNetworkError(ResponseHandler<ErrorCodes, Quiz> handler) {
            handler.handleErrors(new ErrorCodes[]{ErrorCodes.UNKNOWN_ERROR});
        }

        InternetQuizRepository() {
            quizService = retrofit.create(InternetQuizService.class);
        }

        @Override
        public void getQuizzes(final QuizResponseHandler handler) {
            quizService.getQuizzes().enqueue(new DefaultMultiRetrofitCallbackHandler(handler));
        }

        @Override
        public void getOwned(Context context, QuizResponseHandler handler) {
            ensureAuthorized(context);
            quizService.getOwned().enqueue(new DefaultMultiRetrofitCallbackHandler(handler));
        }

        @Override
        public void getQuizByID(String id, final QuizResponseHandler handler) {
            quizService.getQuizByID(id).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void createQuiz(Context context, Quiz quiz, final QuizResponseHandler handler) {
            ensureAuthorized(context);
            quizService.createQuiz(quiz).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void editQuiz(Context context, String id, Quiz quiz, QuizResponseHandler handler) {
            ensureAuthorized(context);
            quizService.editQuiz(id, quiz).enqueue(new DefaultRetrofitCallbackHandler(handler));
        }

        @Override
        public void deleteQuiz(Context context, Quiz quiz, final QuizResponseHandler handler) {
            ensureAuthorized(context);
            quizService.deleteQuiz(quiz.id).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    handler.handle(null);
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    forwardNetworkError(handler);
                }
            });
        }

        interface InternetQuizService {
            @GET("quizzes/")
            Call<List<Quiz>> getQuizzes();

            @GET("quizzes/owned")
            Call<List<Quiz>> getOwned();

            @GET("quizzes/{id}")
            Call<Quiz> getQuizByID(@Path("id") String id);

            @POST("quizzes/")
            Call<Quiz> createQuiz(@Body Quiz quiz);

            @DELETE("quizzes/{id}")
            Call<ResponseBody> deleteQuiz(@Path("id") String id);

            @PUT("quizzes/{id}")
            Call<Quiz> editQuiz(@Path("id") String id, @Body Quiz quiz);
        }
    }

    public static class LocalQuizRepository implements QuizService {
        private final static Map<String, Quiz> localQuizzes = new ConcurrentHashMap<>();
        private final Gson gson;
        private Context context;
        private QuizService onlineService;

        public LocalQuizRepository(Context context, QuizService onlineService) {
            this.context = context;
            this.onlineService = onlineService;
            String saved = context.getSharedPreferences("savedQuizzes", Context.MODE_PRIVATE).getString("quizzes", "{}");
            gson = new GsonBuilder().create();
            Type mapType = new TypeToken<Map<String, Quiz>>() {
            }.getType();
            //noinspection ConstantConditions
            localQuizzes.putAll(gson.fromJson(saved, mapType));
        }

        public void save() {
            context.getSharedPreferences("savedQuizzes", Context.MODE_PRIVATE).edit().putString("quizzes", gson.toJson(localQuizzes)).apply();
        }

        private List<Quiz> appendLocalToQuizTitles(Collection<Quiz> quizzes) {
            return StreamSupport.stream(quizzes).map(Quiz::new).map(q -> {
                q.title += " (local)";
                q.isLocal = true;
                return q;
            }).collect(Collectors.toList());
        }

        @Override
        public void getQuizzes(QuizResponseHandler handler) {
            onlineService.getQuizzes(new QuizResponseHandler() {
                @Override
                public void handleMultiple(List<Quiz> quizzes) {
                    // Necessary in case that the list returned by the online service is immutable
                    // Append (local) to all local quiz titles
                    List<Quiz> toRet = new ArrayList<>(
                            appendLocalToQuizTitles(localQuizzes.values()));
                    toRet.addAll(quizzes);
                    handler.handleMultiple(toRet);
                }

                @Override
                public void handleErrors(@NonNull ErrorCodes... errors) {
                    handler.handleMultiple(new ArrayList<>(appendLocalToQuizTitles(localQuizzes.values())));
                }
            });
        }

        @Override
        public void getOwned(Context context, QuizResponseHandler handler) {
            onlineService.getOwned(context, new QuizResponseHandler() {
                @Override
                public void handleMultiple(List<Quiz> quizzes) {
                    //Necessary in case that the list returned by the online service is immutable
                    List<Quiz> toRet = new ArrayList<>(appendLocalToQuizTitles(localQuizzes.values()));
                    toRet.addAll(quizzes);
                    handler.handleMultiple(toRet);
                }

                @Override
                public void handleErrors(@NonNull ErrorCodes... errors) {
                    handler.handleMultiple(new ArrayList<>(appendLocalToQuizTitles(localQuizzes.values())));
                }
            });
        }

        @Override
        public void getQuizByID(String id, QuizResponseHandler handler) {
            if (localQuizzes.containsKey(id)) {
                Quiz local = new Quiz(localQuizzes.get(id));
                local.isLocal = true;
                handler.handle(local);
                return;
            }
            onlineService.getQuizByID(id, handler);
        }

        private void processQuiz(Quiz quiz) {
            StreamSupport.stream(quiz.questions)
                    .forEach(q -> StreamSupport.stream(q.answers)
                            .forEach(a -> a.id = UUID.randomUUID().toString()));
        }

        @Override
        public void createQuiz(Context context, Quiz quiz, QuizResponseHandler handler) {
            onlineService.createQuiz(context, quiz, new QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    handler.handle(quiz);
                }

                @Override
                public void handleErrors(@NonNull ErrorCodes... errors) {
                    Toast.makeText(LocalQuizRepository.this.context, "Failed to save online. Saved locally.", Toast.LENGTH_SHORT).show();
                    if (quiz.id == null) {
                        quiz.id = UUID.randomUUID().toString();
                    }
                    processQuiz(quiz);
                    localQuizzes.put(quiz.id, quiz);
                    LocalQuizRepository.this.save();
                    handler.handle(quiz);
                }
            });
        }

        @Override
        public void editQuiz(Context context, String id, Quiz quiz, QuizResponseHandler handler) {
            onlineService.editQuiz(context, id, quiz, new QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    handler.handle(quiz);
                }

                @Override
                public void handleErrors(@NonNull ErrorCodes... errors) {
                    //If failed to edit quiz online, it is likely that the quiz was created locally
                    // and needs to be created on the server as well
                    if (localQuizzes.containsKey(id)) {
                        createQuiz(LocalQuizRepository.this.context, quiz, handler);
                        return;
                    }

                    handler.handleErrors(errors);
                }
            });
        }

        @Override
        public void deleteQuiz(Context context, Quiz quiz, QuizResponseHandler handler) {
            //Delete locally as well
            localQuizzes.remove(quiz.id);
            onlineService.deleteQuiz(context, quiz, new QuizResponseHandler() {
                @Override
                public void handle(Quiz quiz) {
                    super.handle(quiz);
                    handler.handle(quiz);
                }

                @Override
                public void handleErrors(@NonNull ErrorCodes... errors) {
                    super.handleErrors(errors);
                    handler.handle(null);
                }
            });
            LocalQuizRepository.this.save();
        }
    }
}
