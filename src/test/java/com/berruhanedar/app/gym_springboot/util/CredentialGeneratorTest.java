package com.berruhanedar.app.gym_springboot.util;

import com.berruhanedar.app.gym_springboot.dao.TraineeDao;
import com.berruhanedar.app.gym_springboot.dao.TrainerDao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CredentialGeneratorTest {

    @Test
    void shouldGenerateBaseUsernameWhenThereIsNoCollision() {
        CredentialGenerator generator = generatorWith(List.of(), List.of());

        String username = generator.generateUsername("  Jane  ", "  Doe  ");

        assertThat(username).isEqualTo("Jane.Doe");
    }

    @Test
    void shouldGenerateNextSuffixAcrossTraineesAndTrainers() {
        CredentialGenerator generator = generatorWith(
                List.of("Jane.Doe", "Jane.Doe2", "Jane.Doe10", "Jane.DoeXYZ"),
                List.of("Jane.Doe1", "Other.User", "Jane.Doe3"));

        String username = generator.generateUsername("Jane", "Doe");

        assertThat(username).isEqualTo("Jane.Doe11");
    }

    @Test
    void shouldIgnoreSimilarUsernamesThatAreNotExactBaseOrNumericSuffix() {
        CredentialGenerator generator = generatorWith(
                List.of("Ann.SmithExtra", "Ann.SmithABC", "Ann.Smith_1"),
                List.of("Ann.Smithson", "Ann.SmithX"));

        String username = generator.generateUsername("Ann", "Smith");

        assertThat(username).isEqualTo("Ann.Smith");
    }

    @Test
    void shouldGenerateTenCharacterAlphanumericPassword() {
        CredentialGenerator generator = generatorWith(List.of(), List.of());

        String password = generator.generatePassword();

        assertThat(password).hasSize(10).matches("[A-Za-z0-9]{10}");
    }

    private CredentialGenerator generatorWith(List<String> traineeUsernames, List<String> trainerUsernames) {
        CredentialGenerator generator = new CredentialGenerator();
        generator.setTraineeDao(new FakeTraineeDao(traineeUsernames));
        generator.setTrainerDao(new FakeTrainerDao(trainerUsernames));
        return generator;
    }

    private static class FakeTraineeDao extends TraineeDao {
        private final List<String> usernames;

        private FakeTraineeDao(List<String> usernames) {
            this.usernames = usernames;
        }

        @Override
        public List<String> findAllUsernames() {
            return usernames;
        }
    }

    private static class FakeTrainerDao extends TrainerDao {
        private final List<String> usernames;

        private FakeTrainerDao(List<String> usernames) {
            this.usernames = usernames;
        }

        @Override
        public List<String> findAllUsernames() {
            return usernames;
        }
    }
}
