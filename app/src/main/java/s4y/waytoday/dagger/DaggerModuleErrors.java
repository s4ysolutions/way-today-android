package s4y.waytoday.dagger;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.errors.ErrorReporter;

@Module()
public class DaggerModuleErrors {
    @Provides
    ErrorReporter provideErrorReporter() {
        return new ErrorReporter();
    }
}
