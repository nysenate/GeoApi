package gov.nysenate.sage.dao.model.assembly;

import gov.nysenate.sage.model.district.Assembly;

import java.util.List;

public interface AssemblyDao {

    /**
     *
     * @return List<Assembly> All current assembly members
     */
    public List<Assembly> getAssemblies();


    /**
     * Retrieves the Assembly member of the district specified
     * @param district
     * @return Assembly object containing info on an Assembly member
     */
    public Assembly getAssemblyByDistrict(int district);

    /**
     * Inserts the Assembly member object into the database
     * @param assembly
     */
    public void insertAssembly(Assembly assembly);

    /**
     * Clears the assembly table.
     */
    public void deleteAssemblies();

    /**
     * Removes an assembly by district.
     */
    public void deleteAssemblies(int district);

}
