@Library('jenkins-vdab-common') _

appName = "gebruikersbeheer-derden"
imageProject = "externe-organisatie"

copsAppPipeline(
    name: appName,
    buildImage: 'artifacts.vdab.be/ops-docker/ci/java25-rh:latest',
    releaseBranch: 'main',
    useSemVer: true,
    generateTag: true,
   	generateJiraTag: true,
    unit: [
        enabled: true,
    ],
    integration: [
        enabled: true,
        envVars: [
            CHROMEDRIVER_ARGS: "--no-sandbox --headless --disable-dev-shm-usage --disable-extensions"
        ]
    ],
    sonar: [
        enabled: ! env.JOB_BASE_NAME.startsWith('renovate')
    ],
    release: [
        enabled: true,
        // don't publish maven artifacts, we only need a container
        publishToNexus: false
    ],
    securityChecks: [
            gitleaks: true
        ],
    dockerFiles: [
	    [
			path: '.',
			imageProject: imageProject,
			imageName: appName,
			repoUpdate: [
			    repository: "cops/deployments/${imageProject}/${appName}",
			    valuesFile: "${appName}/values.yaml",
			    yamlPath: ".${appName}.image.tag"
			]
        ]
	]
)
