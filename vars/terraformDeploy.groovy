def call(Map config) {
    def tfDir     = config.dir      ?: '.'
    def workspace = config.workspace ?: 'default'
    def varFile   = config.varFile  ?: ''
    def action    = config.action   ?: 'apply'

    dir(tfDir) {
        sh 'terraform init'
        sh "terraform workspace new ${workspace} || terraform workspace select ${workspace}"

        if (action == 'plan') {
            sh "terraform plan ${varFile ? \"-var-file=${varFile}\" : ''} -out=tfplan"

        } else if (action == 'apply') {
            sh "terraform apply -auto-approve ${varFile ? \"-var-file=${varFile}\" : ''}"

        } else if (action == 'destroy') {
            sh "terraform destroy -auto-approve ${varFile ? \"-var-file=${varFile}\" : ''}"
        }
    }
}

