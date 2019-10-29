package com.qaprosoft.jenkins.jobdsl.factory.pipeline

import com.qaprosoft.jenkins.jobdsl.factory.job.JobFactory
import groovy.transform.InheritConstructors
import org.apache.tools.ant.types.resources.selectors.None

@InheritConstructors
public class PipelineFactory extends JobFactory {
    def pipelineScript = ""
    def suiteOwner = ""

    public PipelineFactory(folder, name, description) {
        super(folder, name, description)
    }

    public PipelineFactory(folder, name, description, logRotator) {
        super(folder, name, description, logRotator)
    }

    public PipelineFactory(folder, name, description, logRotator, pipelineScript) {
        super(folder, name, description, logRotator)
        this.pipelineScript = pipelineScript
    }

    public PipelineFactory(folder, name, description, logRotator, pipelineScript, suiteOwner) {
        super(folder, name, description, logRotator)
        this.pipelineScript = pipelineScript
        this.suiteOwner = suiteOwner
    }

    public PipelineFactory(folder, name, description, logRotator, suiteOwner, isJenkinsfile, remoteRepoURL, remoteBranch, remoteCredentials) {
        super(folder, name, description, logRotator)
        this.pipelineScript = pipelineScript
        this.suiteOwner = suiteOwner
        this.isJenkinsfile = isJenkinsfile
        this.remoteRepoURL = remoteRepoURL
        this.remoteBranch = remoteBranch
        this.remoteCredentials = remoteCredentials
    }

    def create() {
        def pipelineJob = _dslFactory.pipelineJob(getFullName()){
            description "${description}"
            logRotator { numToKeep logRotator }

            authenticationToken('ciStart')

            properties {
                disableResume()
                durabilityHint { hint("PERFORMANCE_OPTIMIZED") }
                if (!suiteOwner.isEmpty()) {
                    ownership { primaryOwnerId(suiteOwner) }
                }
            }

            /** Git Stuff **/
            definition {
                if (isJenkinsfile) {
                    cpsScm {
                        scm {
                            remoteRepoURL = 'https://github.com/qaprosoft/carina-demo'
                            remoteBranch = 'dsl'
                            remoteCredentials = None

                            git {
                                branch(remoteBranch)
                            }
                            remote {
                                credentials(remoteCredentials)
                                url(remoteRepoURL)
                            }
                        }
                    }
                } else {
                    cps {
                        script(pipelineScript)
                        sandbox()
                    }
                }
            }
        }
        return pipelineJob
    }

    protected List<String> getEnvironments(currentSuite) {
        def envList = getGenericSplit(currentSuite, "jenkinsEnvironments")

        if (envList.isEmpty()) {
            envList.add("DEMO")
            envList.add("STAG")
        }

        return envList
    }

    protected List<String> getGenericSplit(currentSuite, parameterName) {
        String genericField = currentSuite.getParameter(parameterName)
        def genericFields = []

        if (genericField != null) {
            if (!genericField.contains(", ")) {
                genericFields = genericField.split(",")
            } else {
                genericFields = genericField.split(", ")
            }
        }
        return genericFields
    }

    protected Closure addHiddenParameter(paramName, paramDesc, paramValue) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'com.wangyin.parameter.WHideParameterDefinition'(plugin: 'hidden-parameter@0.0.4') {
                name paramName
                description paramDesc
                defaultValue paramValue
            }
        }
    }

    protected Closure addExtensibleChoice(choiceName, globalName, desc, choice) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.3.3') {
                name choiceName
                description desc
                editable true
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.GlobalTextareaChoiceListProvider') {
                    whenToAdd 'Triggered'
                    name globalName
                    defaultChoice choice
                }
            }
        }
    }

    protected Closure addExtensibleChoice(choiceName, desc, code) {
        return { node ->
            node / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'jp.ikedam.jenkins.plugins.extensible__choice__parameter.ExtensibleChoiceParameterDefinition'(plugin: 'extensible-choice-parameter@1.3.3') {
                name choiceName
                description desc
                editable true
                choiceListProvider(class: 'jp.ikedam.jenkins.plugins.extensible_choice_parameter.SystemGroovyChoiceListProvider') {
                    groovyScript {
                        script code
                        sandbox true
                        usePrefinedVariables false
                    }
                }
            }
        }
    }

}