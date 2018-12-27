package me.srikavin.quiz.repository;

public enum GameRepository {
//    INSTANCE;
//
//    private InternetQuizRepository internetQuizRepository = new InternetQuizRepository();
//
//    public LiveData<List<Quiz>> getQuizzes() {
//        return internetQuizRepository.fetch();
//    }
//
//    interface QuizService {
//        LiveData<List<Quiz>> fetch();
//    }
//
//    static class InternetQuizRepository extends InternetRepository implements QuizService {
//
//        private final InternetQuizService quizService;
//
//        InternetQuizRepository() {
//            quizService = retrofit.create(InternetQuizService.class);
//        }
//
//        @Override
//        public LiveData<List<Quiz>> fetch() {
//            final MutableLiveData<List<Quiz>> liveData = new MutableLiveData<>();
//            quizService.getQuizzes().enqueue(new Callback<List<Quiz>>() {
//                @Override
//                public void onResponse(Call<List<Quiz>> call, Response<List<Quiz>> response) {
//                    liveData.postValue(response.body());
//                }
//
//                public void onFailure(Call<List<Quiz>> call, Throwable t) {
//                    liveData.postValue(null);
//                }
//            });
//
//            return liveData;
//        }
//
//        interface InternetQuizService {
//            @GET("quizzes/")
//            Call<List<Quiz>> getQuizzes();
//
//            @GET("quizzes/{id}")
//            Call<List<Quiz>> getQuizByID(@Path("id") UUID id);
//        }
//
//        private class QuizService {
//
//        }
//    }
//
//    public static class LocalQuizRepository {
//    }
//
//    public static class WifiDirectQuizRepository {
//    }
}
