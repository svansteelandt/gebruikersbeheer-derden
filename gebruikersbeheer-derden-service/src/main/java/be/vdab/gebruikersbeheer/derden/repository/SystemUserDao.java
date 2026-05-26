package be.vdab.gebruikersbeheer.derden.repository;


import com.ibm.itim.ws.model.session.WSSession;
import com.ibm.itim.ws.model.system.user.WSSystemUser;
import com.ibm.itim.ws.services.system.user.WSSystemUserService;

public interface SystemUserDao {

	WSSystemUser findSystemUser(WSSession session, WSSystemUserService systemUserService);

}
