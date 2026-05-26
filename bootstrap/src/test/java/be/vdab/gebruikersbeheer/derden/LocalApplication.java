package be.vdab.gebruikersbeheer.derden;

import be.vdab.testcontainers.ReuseMode;
import be.vdab.testcontainers.VdabContainers;
import be.vdab.testcontainers.keycloak.VdabKeycloakContainer;
import org.springframework.boot.SpringApplication;
import org.testcontainers.containers.GenericContainer;

import java.util.List;

import static be.vdab.testcontainers.keycloak.dsl.KeycloakUtil.realmBuilderInterne;

public class LocalApplication {

	private static final String REALM = "ISAM";
	private static final String CLIENT = "gebruikersbeheer-derden";
	private static final String CLIENT_SECRET = "gebruikersbeheer-derden-123";
	private final VdabKeycloakContainer keycloakContainer;

	LocalApplication() {
		keycloakContainer = new VdabKeycloakContainer()
				.withPortBindings("8081:8080")
				.withRealms(
						realmBuilderInterne(REALM)
								.client(CLIENT, CLIENT_SECRET)
								.user("avhoye", "avhoye",
										user -> user.roles("Beheerders Derden zonder RRN", "CVS_rfi"))
								.build()
				);

		VdabContainers
				.register(keycloakContainer)
				.withReuse(ReuseMode.PERMANENT)
				.start();

		System.setProperty("oidc.issuers.intern.url", "http://localhost:8081/realms/ISAM");

		getTokens();
	}

	private void getTokens() {
		System.out.println("KEYCLOAK ACCESS TOKENS");
		System.out.println("======================");
		System.err.println("avhoye: " + keycloakContainer.getAccessTokenForInterne(CLIENT, "avhoye"));
		System.out.println("======================");
	}

	protected List<GenericContainer<?>> getContainers() {
		return List.of(keycloakContainer.getTestcontainer());
	}

	private void setShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> getContainers().forEach(GenericContainer::stop)));
	}

	public void start(String[] args) {
		System.setProperty("spring.profiles.active", "local");
		setShutdownHook();

		SpringApplication
				.from(SpringBootWebApplication::main)
				.run(args);
	}

	public static void main(String[] args) {
		LocalApplication application = new LocalApplication();
		application.start(args);
	}
}
