package gov.nysenate.sage.dao.model.assembly;

import gov.nysenate.sage.model.district.Assembly;

import java.util.List;

public interface AssemblyDao {
    /**
     *
     * @return List<Assembly> All current assembly members
     */
    List<Assembly> getAssemblies();


    /**
     * Retrieves the Assembly member of the district specified
     * @return Assembly object containing info on an Assembly member
     */
    Assembly getAssemblyByDistrict(int district);

    /**
     * Inserts the Assembly member object into the database
     */
    void insertAssembly(Assembly assembly);

    /**
     * Removes an assembly by district.
     */
    void deleteAssemblies(int district);
}
