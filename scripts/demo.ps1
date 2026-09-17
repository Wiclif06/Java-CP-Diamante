param(
    [string]$ApiBase = "http://localhost:8080",
    [string]$AdminEmail = "",
    [string]$AdminPassword = ""
)

$ErrorActionPreference = "Stop"
$results = [System.Collections.Generic.List[object]]::new()

function Invoke-Api {
    param([string]$Method, [string]$Path, [object]$Body = $null, [string]$Token = "")
    $headers = @{}
    if ($Token) { $headers.Authorization = "Bearer $Token" }
    $parameters = @{
        Uri = "$ApiBase$Path"
        Method = $Method
        Headers = $headers
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $parameters.ContentType = "application/json"
        $parameters.Body = ($Body | ConvertTo-Json -Depth 5 -Compress)
    }
    try {
        $response = Invoke-WebRequest @parameters
        $parsed = if ($response.Content) { $response.Content | ConvertFrom-Json } else { $null }
        return [pscustomobject]@{ Status = [int]$response.StatusCode; Body = $parsed }
    } catch {
        $status = 0
        $content = $null
        if ($_.Exception.Response) {
            $status = [int]$_.Exception.Response.StatusCode
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = [System.IO.StreamReader]::new($stream)
                $raw = $reader.ReadToEnd()
                if ($raw) { $content = $raw | ConvertFrom-Json }
            } catch {}
        }
        return [pscustomobject]@{ Status = $status; Body = $content }
    }
}

function Add-Evidence {
    param([string]$Case, [int]$Expected, [object]$Response)
    $results.Add([pscustomobject]@{
        case = $Case
        expectedStatus = $Expected
        observedStatus = $Response.Status
        passed = ($Response.Status -eq $Expected)
        responseCode = $Response.Body.code
    })
    $mark = if ($Response.Status -eq $Expected) { "OK" } else { "FALHOU" }
    Write-Host "[$mark] $Case - esperado $Expected, obtido $($Response.Status)"
}

$suffix = [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$password = "SenhaForte123!"
$emailA = "ana.$suffix@campus.test"
$emailB = "bia.$suffix@campus.test"

$registerA = Invoke-Api POST "/api/auth/register" @{
    name = "Ana"; email = $emailA; password = $password; cep = "01001000"
}
Add-Evidence "Cadastro A com CEP válido" 201 $registerA

$registerB = Invoke-Api POST "/api/auth/register" @{
    name = "Bia"; email = $emailB; password = $password; cep = "30140071"
}
Add-Evidence "Cadastro B com CEP válido" 201 $registerB

$loginA = Invoke-Api POST "/api/auth/login" @{ email = $emailA; password = $password }
Add-Evidence "Login A" 200 $loginA
$loginB = Invoke-Api POST "/api/auth/login" @{ email = $emailB; password = $password }
Add-Evidence "Login B" 200 $loginB
if (!$loginA.Body.accessToken -or !$loginB.Body.accessToken) {
    throw "Não foi possível obter os tokens. Corrija cadastro/login antes de continuar."
}
$tokenA = $loginA.Body.accessToken
$tokenB = $loginB.Body.accessToken

$publish = Invoke-Api POST "/api/services" @{
    title = "Revisão de Java"; description = "Aula de revisão para a prova"
    category = "EDUCACAO"; price = 40.00
} $tokenA
Add-Evidence "A publica serviço" 201 $publish
$serviceId = $publish.Body.id
if (!$serviceId) { throw "Publicação não devolveu id." }

$hireB = Invoke-Api POST "/api/services/$serviceId/contracts" $null $tokenB
Add-Evidence "B contrata serviço de A" 201 $hireB

$withoutToken = Invoke-Api POST "/api/services" @{
    title = "Sem token"; description = "Deve falhar"; category = "TESTE"; price = 1.00
}
Add-Evidence "Alteração sem token" 401 $withoutToken

$invalidToken = Invoke-Api POST "/api/services" @{
    title = "Token adulterado"; description = "Deve falhar"; category = "TESTE"; price = 1.00
} "token-invalido"
Add-Evidence "Alteração com token adulterado" 401 $invalidToken

$closeByB = Invoke-Api PATCH "/api/services/$serviceId/close" $null $tokenB
Add-Evidence "B tenta encerrar serviço de A" 403 $closeByB

$selfHire = Invoke-Api POST "/api/services/$serviceId/contracts" $null $tokenA
Add-Evidence "A tenta contratar o próprio serviço" 409 $selfHire

$closeByA = Invoke-Api PATCH "/api/services/$serviceId/close" $null $tokenA
Add-Evidence "A encerra o próprio serviço" 200 $closeByA

$hireClosed = Invoke-Api POST "/api/services/$serviceId/contracts" $null $tokenB
Add-Evidence "B tenta contratar serviço encerrado" 409 $hireClosed

$invalidCep = Invoke-Api POST "/api/auth/register" @{
    name = "CEP inválido"; email = "cep.$suffix@campus.test"; password = $password; cep = "99999999"
}
Add-Evidence "Cadastro com CEP inexistente" 422 $invalidCep

if ($AdminEmail -and $AdminPassword) {
    $adminLogin = Invoke-Api POST "/api/auth/login" @{ email = $AdminEmail; password = $AdminPassword }
    Add-Evidence "Login ADMIN" 200 $adminLogin
    if ($adminLogin.Body.accessToken) {
        $publish2 = Invoke-Api POST "/api/services" @{
            title = "Serviço para ADMIN encerrar"; description = "Teste de papel"
            category = "TESTE"; price = 10.00
        } $tokenA
        Add-Evidence "A publica segundo serviço" 201 $publish2
        $adminClose = Invoke-Api PATCH "/api/services/$($publish2.Body.id)/close" $null $adminLogin.Body.accessToken
        Add-Evidence "ADMIN encerra serviço de A" 200 $adminClose
    }
}

$evidence = [pscustomobject]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    apiBase = $ApiBase
    serviceId = $serviceId
    tests = $results
    totals = [pscustomobject]@{
        executed = $results.Count
        passed = @($results | Where-Object passed).Count
        failed = @($results | Where-Object { -not $_.passed }).Count
    }
}
$output = Join-Path $PSScriptRoot "..\docs\evidencia-manual.json"
$evidence | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $output -Encoding UTF8
Write-Host "Evidência gravada em $output (sem senhas e sem tokens)."
if ($evidence.totals.failed -gt 0) { exit 1 }
