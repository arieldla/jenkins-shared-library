package com.myorg

class TerraformHelper implements Serializable {

    def script

    TerraformHelper(script) {
        this.script = script
    }

    def getWorkspaces() {
        return script.sh(
            script: 'terraform workspace list',
            returnStdout: true
        ).trim()
    }

    def currentWorkspace() {
        return script.sh(
            script: 'terraform workspace show',
            returnStdout: true
        ).trim()
    }
}
