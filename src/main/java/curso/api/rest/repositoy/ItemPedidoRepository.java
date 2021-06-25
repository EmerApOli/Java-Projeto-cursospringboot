package curso.api.rest.repositoy;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import curso.api.rest.model.ItemPedido;
import curso.api.rest.model.Pedido;
//import curso.api.rest.model.Telefone;

@Repository
public interface ItemPedidoRepository extends CrudRepository<ItemPedido, Long> {

	
}
