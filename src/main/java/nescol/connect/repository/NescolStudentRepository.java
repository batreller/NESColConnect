package nescol.connect.repository;

import nescol.connect.model.NescolStudent;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;


public interface NescolStudentRepository extends CrudRepository<NescolStudent, String> {
    @Modifying
    @Query("update NescolStudent n set n.registered=true where n.studentId = :studentId and n.name = :name and n.surname = :surname and n.registered = false")
    int updateUnusedStudent(
            @Param("studentId") String studentId,
            @Param("name") String name,
            @Param("surname") String surname
    );
}
