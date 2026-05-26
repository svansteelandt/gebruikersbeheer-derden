package be.vdab.gebruikersbeheer.derden.repository;


import be.vdab.gebruikersbeheer.util.exception.IsimApplicationException;
import com.ibm.itim.ws.model.session.WSSession;
import com.ibm.itim.ws.model.system.user.WSSystemUser;
import com.ibm.itim.ws.services.WSApplicationException;
import com.ibm.itim.ws.services.WSLoginServiceException;
import com.ibm.itim.ws.services.system.user.WSSystemUserService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class SystemUserDaoImpl implements SystemUserDao {

	public WSSystemUser findSystemUser(WSSession session, WSSystemUserService systemUserService) {
		if (log.isDebugEnabled()){
			log.debug("findSystemUser");
		}

		try {
			return systemUserService.getSystemUser(session);
		}catch(WSApplicationException ex){
			log.error("WSApplicationException occurred", ex);
			throw new IsimApplicationException("WSApplicationException occurred", ex);
		}catch(WSLoginServiceException ex){
			log.error("WSLoginServiceException occurred", ex);
			throw new IsimApplicationException("WSLoginServiceException occurred", ex);
		}
	}
}
