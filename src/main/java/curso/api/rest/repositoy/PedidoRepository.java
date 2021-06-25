package curso.api.rest.repositoy;

import java.util.List;

import javax.xml.crypto.Data;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Pedido;
//import curso.api.rest.model.Telefone;
import curso.api.rest.model.Usuario;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
	

	
	
	
	@Query("select p from Pedido p where p.nome like %?1%")
	List<Pedido> findPedidoByNome(String nome);
	
	
	@Query(value="SELECT constraint_name from information_schema.constraint_column_usage  where table_name = 'usuarios_role' and column_name = 'role_id' and constraint_name <> 'unique_role_user';", nativeQuery = true)
	String consultaConstraintRole();
	
	@Transactional
	@Modifying
	@Query(nativeQuery = true, value = "insert into usuarios_role (usuario_id, role_id) values(?1, (select id from role where nome_role = 'ROLE_USER')); ")
	void insereAcessoRolePadrao(Long idUser);

	
	@Transactional
	@Modifying
	@Query(value = "update usuario set senha = ?1 where id = ?2", nativeQuery = true)
	void updateSenha(String senha, Long codUser);


	default Page<Pedido> findPedidoByNamePage(String nome, PageRequest pageRequest) {
		
		Pedido pedido = new Pedido();
		pedido.setNome(nome);
		
		/*Configurando para pesquisar por nome e paginação*/
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
				.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers
						.contains().ignoreCase());
		
		Example<Pedido> example = Example.of(pedido, exampleMatcher);
		
		Page<Pedido> retorno = findAll(example, pageRequest);
		
		return retorno;
		
	}

}
