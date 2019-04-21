package s4y.waytoday;

import dagger.Module;
import dagger.Provides;
import s4y.waytoday.errors.ErrorReporter;

import static org.mockito.Mockito.mock;

@Module()
class DaggerTestErrorsModule {
    @Provides
    ErrorReporter provideErrorReporter() {
        return mock(ErrorReporter.class);
    }
}
