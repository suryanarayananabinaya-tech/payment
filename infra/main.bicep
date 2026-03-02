@description('Location for all resources')
param location string = resourceGroup().location

@description('AKS cluster name')
param aksName string

@description('ACR name (must be globally unique, 5-50 alphanumeric)')
param acrName string

@description('Key Vault name (must be globally unique)')
param kvName string

@description('AKS DNS prefix')
param dnsPrefix string = 'payaksdev'

@description('Node count')
param nodeCount int = 2

@description('Node VM size')
param nodeVmSize string = 'Standard_DS2_v2'

@description('Log Analytics retention in days')
param logRetentionDays int = 14

// ---------------------------
// Log Analytics (for Container Insights)
// ---------------------------
resource law 'Microsoft.OperationalInsights/workspaces@2023-09-01' = {
  name: '${aksName}-law'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: logRetentionDays
  }
}

// ---------------------------
// Azure Container Registry
// ---------------------------
resource acr 'Microsoft.ContainerRegistry/registries@2023-07-01' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: false
  }
}

// ---------------------------
// Key Vault (RBAC enabled)
// ---------------------------
resource kv 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: kvName
  location: location
  properties: {
    tenantId: subscription().tenantId
    enableRbacAuthorization: true
    sku: {
      family: 'A'
      name: 'standard'
    }
    // No accessPolicies when RBAC is enabled
    accessPolicies: []
  }
}

// ---------------------------
// AKS (OIDC + Workload Identity + KeyVault CSI addon + Container Insights)
// ---------------------------
resource aks 'Microsoft.ContainerService/managedClusters@2024-03-02-preview' = {
  name: aksName
  location: location
  identity: {
    type: 'SystemAssigned'
  }
  properties: {
    dnsPrefix: dnsPrefix

    // Enable OIDC issuer (required for workload identity)
    oidcIssuerProfile: {
      enabled: true
    }

    // Enable Workload Identity
    securityProfile: {
      workloadIdentity: {
        enabled: true
      }
    }

    agentPoolProfiles: [
      {
        name: 'system'
        mode: 'System'
        count: nodeCount
        vmSize: nodeVmSize
        osType: 'Linux'
        type: 'VirtualMachineScaleSets'
      }
    ]

    // Addons
    addonProfiles: {
      // Container Insights -> Log Analytics
      omsagent: {
        enabled: true
        config: {
          logAnalyticsWorkspaceResourceID: law.id
        }
      }

      // Key Vault Secrets Store CSI provider addon
      azureKeyvaultSecretsProvider: {
        enabled: true
      }
    }

    networkProfile: {
      networkPlugin: 'azure'
      loadBalancerSku: 'standard'
    }
  }
}

// ---------------------------
// Allow AKS kubelet identity to pull images from ACR
// ---------------------------
var kubeletObjectId = aks.properties.identityProfile.kubeletidentity.objectId

resource acrPullRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(acr.id, kubeletObjectId, 'acrpull')
  scope: acr
  properties: {
    // Built-in role: AcrPull
    roleDefinitionId: subscriptionResourceId(
      'Microsoft.Authorization/roleDefinitions',
      '7f951dda-4ed3-4680-a7ca-43fe172d538d'
    )
    principalId: kubeletObjectId
    principalType: 'ServicePrincipal'
  }
}

// ---------------------------
// Outputs
// ---------------------------
output acrLoginServer string = acr.properties.loginServer
output aksNameOut string = aks.name
output keyVaultName string = kv.name
output tenantId string = subscription().tenantId
output logAnalyticsWorkspaceId string = law.id
output oidcIssuerUrl string = aks.properties.oidcIssuerProfile.issuerUrl