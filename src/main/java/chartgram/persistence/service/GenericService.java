package chartgram.persistence.service;

import chartgram.persistence.repository.GenericRepository;

import java.util.ArrayList;
import java.util.List;

public class GenericService<E> {
	private final GenericRepository<E> genericRepository;

	protected GenericService(GenericRepository<E> genericRepository) {
		this.genericRepository = genericRepository;
	}

	protected GenericRepository<E> getRepository() {
		return genericRepository;
	}

	public List<E> list() {
		List<E> result = new ArrayList<>();
		getRepository().findAll().forEach(result::add);
		return result;
	}

	public E add(E entity) {
		return getRepository().save(entity);
	}

	public void delete(E entity) {
		getRepository().delete(entity);
	}

	public E findById(long id) {
		return getRepository().findById(id).orElse(null);
	}

	public boolean existsById(long id) {
		return getRepository().existsById(id);
	}
}
