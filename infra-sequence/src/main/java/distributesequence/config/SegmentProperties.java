package distributesequence.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * @author peanut
 * @description
 */
public class SegmentProperties {

    @Value("${peanut.sequence.segment.enabled:true}")
    private boolean sequenceSegmentEnabled;

    @Value("${peanut.sequence.jdbc.url:}")
    private String sequenceJdbcUrl;

    @Value("${peanut.sequence.jdbc.username:}")
    private String sequenceJdbcUserName;

    @Value("${peanut.sequence.jdbc.password:}")
    private String sequenceJdbcPassWord;

    public boolean isSequenceSegmentEnabled() {
        return sequenceSegmentEnabled;
    }

    public String getSequenceJdbcUrl() {
        return sequenceJdbcUrl;
    }

    public String getSequenceJdbcUserName() {
        return sequenceJdbcUserName;
    }

    public String getSequenceJdbcPassWord() {
        return sequenceJdbcPassWord;
    }
}
