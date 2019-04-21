package s4y.waytoday.errors;

import dagger.Module;
import dagger.Provides;

@Module()
public class DaggerErrorsModule {
    @Provides
    ErrorReporter provideErrorReporter() {
        return new ErrorReporter();
    }
}
