package solutions.s4y.waytoday.errors;

import dagger.Module;
import dagger.Provides;

@Module()
public class ErrorsModule {
    @Provides
    ErrorReporter provideErrorReporter() {
        return new ErrorReporter();
    }
}
