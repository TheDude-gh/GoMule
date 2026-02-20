package gomule.skills;

import randall.d2files.D2TxtFile;
import randall.d2files.D2TxtFileItemProperties;

public class SkillsHelpers {
    private SkillsHelpers() {
    }

    public static D2TxtFileItemProperties getSkillsRowForId(int id) {
        return D2TxtFile.SKILLS.searchColumns("*Id", String.valueOf(id));
    }

    public static D2TxtFileItemProperties getSkillDescRow(String skilldesc) {
        return D2TxtFile.SKILL_DESC.searchColumns("skilldesc", skilldesc);
    }

    public static D2TxtFileItemProperties getSkillDescRowForId(int id) {
        return D2TxtFile.SKILL_DESC.searchColumns("skilldesc", getSkillsRowForId(id).get("skilldesc"));
    }
}
