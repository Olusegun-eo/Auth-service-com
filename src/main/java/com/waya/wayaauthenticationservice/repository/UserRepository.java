package com.waya.wayaauthenticationservice.repository;

import com.waya.wayaauthenticationservice.entity.Role;
import com.waya.wayaauthenticationservice.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

	@Query(value = "SELECT * FROM m_users u WHERE UPPER(u.email) = UPPER(:value) AND u.is_deleted = false", nativeQuery = true)
	Optional<Users> findByEmailIgnoreCase(@Param("value") String value);

	@Query("SELECT u FROM Users u WHERE u.phoneNumber LIKE CONCAT('%', ?1) AND u.isDeleted = false")
	Optional<Users> findByPhoneNumber(String phoneNumber);

	@Query(value = "SELECT u FROM Users u WHERE (UPPER(u.email) = UPPER(:value) OR "
			+ "u.phoneNumber LIKE CONCAT('%', :value)) AND u.isDeleted = false")
//    @Query(value = "SELECT * FROM m_users u WHERE (UPPER(u.email) = UPPER(:value) OR " +
//            "u.phone_number LIKE CONCAT('%', :value)) AND u.is_deleted = false", nativeQuery = true)
	Optional<Users> findByEmailOrPhoneNumber(@Param("value") String value);

	Page<Users> findByRoleListIn(Collection<Role> roles, Pageable pageable);

	List<Users> findByRoleList_(Role role);

	@Query(value = "SELECT u FROM Users u WHERE u.isCorporate = :value AND u.isDeleted = false ORDER BY id")
	Page<Users> findUserByIsCorporate(@Param("value") boolean value, Pageable pageable);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u "
			+ "WHERE UPPER(u.email) = UPPER(:email) AND u.isDeleted = false")
	boolean existsByEmail(@Param("email") String email);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u "
			+ "WHERE u.phoneNumber LIKE CONCAT('%', :value) AND u.isDeleted = false")
	boolean existsByPhoneNumber(@Param("value") String value);

	@Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM Users u "
			+ "WHERE UPPER(u.email) = UPPER(:value) OR "
			+ "u.phoneNumber LIKE CONCAT('%', :value) AND u.isDeleted = false")
	boolean existsByEmailOrPhoneNumber(String value);

	@Override
	@Query(value = "SELECT u FROM Users u WHERE u.id =:userId AND u.isDeleted = false")
	Optional<Users> findById(@Param("userId") Long userId);

	@Query(value = "SELECT u FROM Users u WHERE u.id =:userId AND u.isDeleted =:deleted")
	Optional<Users> findById(@Param("deleted") boolean deleted, @Param("userId") Long userId);

	@Query(value = "update m_users set is_deleted =:deleted where id=:userId", nativeQuery = true)
	Optional<Users> deleteAccountByUserId(boolean deleted, Long userId);

	@Override
	@Query(value = "SELECT u FROM Users u WHERE u.isDeleted = false ORDER BY id")
	Page<Users> findAll(Pageable pageable);
	
	@Query(value = "SELECT u FROM Users u WHERE UPPER(u.email) = UPPER(:email) AND u.isDeleted = false")
	Optional<Users> findSimulatedUser(@Param("email") String email);
	
	@Query(value = "SELECT u FROM Users u WHERE u.isDeleted = false AND u.isSimulated = true AND u.phoneNumber not like '234%' AND phone_number Not like '08%' ORDER BY id")
	Page<Users> findAllSimulated(Specification<Users> spec, Pageable pageable);

	@Query("select count(u.id) from Users u where u.isActive = true and u.isCorporate = false and u.isSimulated = false and u.isDeleted = false and u.isDeleted = false")
	long findByActiveUsers();

	@Query("select count(u.id) from Users u where u.isActive = false and u.isCorporate = false and u.isSimulated = false")
	long findByInactiveUsers();

	@Query("select count(u.id) from Users u where u.isActive = false and u.isCorporate = true and u.isSimulated = false and u.isDeleted = false")
	long findByInactiveCooperateUsers();

	@Query("select count(u.id) from Users u where u.isActive = true and u.isCorporate = true and u.isSimulated = false and u.isDeleted = false ")
	long findByActiveCooperateUsers();

}
