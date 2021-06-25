package curso.api.rest.repositoy;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import curso.api.rest.model.Fornecedor;
import curso.api.rest.model.Produto;
import curso.api.rest.model.Usuario;


@Repository
public interface ProdutoRepository  extends JpaRepository<Produto, Long>{
	
	@Query("select p from Produto p where p.nome like %?1%")
	List<Produto> findProdutoByNome(String nome);	
	

	

	

	
	
	default Page<Produto> findProdutoByNamePage(String nome, PageRequest pageRequest) {
		
		Produto produto = new Produto();
		produto.setNome(nome);
		
		/*Configurando para pesquisar por nome e paginação*/
		ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
				.withMatcher("nome", ExampleMatcher.GenericPropertyMatchers
						.contains().ignoreCase());
		
		Example<Produto> example = Example.of(produto, exampleMatcher);
		
		Page<Produto> retorno = findAll(example, pageRequest);
		
		return retorno;
		
	}

}
