package uz.uzinfocom.app.modules.airquality.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AirParameterTest {

    @Test
    void differentSpellingsOfOnePollutantShareOneCode() {
        assertThat(AirParameter.normalize("PM 2.5")).isEqualTo("PM2.5");
        assertThat(AirParameter.normalize(" pm2.5 ")).isEqualTo("PM2.5");
        assertThat(AirParameter.normalize("NOx-2")).isEqualTo("NOX-2");
        assertThat(AirParameter.normalize("NOx")).isNotEqualTo(AirParameter.normalize("NOx-2"));
    }

    @Test
    void blankNamesHaveNoCode() {
        assertThat(AirParameter.normalize(null)).isNull();
        assertThat(AirParameter.normalize("   ")).isNull();
    }
}
