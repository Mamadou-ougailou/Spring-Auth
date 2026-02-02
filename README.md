# Spring-Auth

##  Description

Spring-Auth est une API REST d'authentification développée avec **Spring Boot**. Ce projet implémente un système d'authentification complet et sécurisé avec gestion des utilisateurs, hashage de mots de passe BCrypt, et système de tokens temporaires.

##  Fonctionnalités

- ✅ **Inscription d'utilisateurs** avec validation des données
- ✅ **Authentification par email et mot de passe** avec hashage BCrypt
- ✅ **Génération de tokens temporaires** (expiration après 1 heure)
- ✅ **Validation de tokens** pour l'autorisation
- ✅ **Révocation de tokens** (déconnexion)
- ✅ **Gestion du profil utilisateur**
- ✅ **Suppression de comptes**
- ✅ **Base de données H2** persistante
- ✅ **Console H2** pour l'administration

##  Technologies utilisées

- **Java 17**
- **Spring Boot 4.0.1**
- **Spring Security** (pour le hashage BCrypt)
- **Spring Data JPA** (pour la persistance)
- **H2 Database** (base de données embarquée)
- **Maven** (gestionnaire de dépendances)

##  Structure du projet

```
Spring-Auth/
├── src/
│   ├── main/
│   │   ├── java/demo/
│   │   │   ├── DemoApp.java                    # Point d'entrée de l'application
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java         # Configuration du password encoder
│   │   │   ├── controller/
│   │   │   │   └── IdentifyController.java     # API endpoints d'authentification
│   │   │   ├── model/
│   │   │   │   ├── Identity.java               # Entité utilisateur
│   │   │   │   ├── Credential.java             # Entité credentials (email/password)
│   │   │   │   └── Autority.java               # Entité token d'authentification
│   │   │   ├── repository/
│   │   │   │   ├── IdentityRepository.java     # Repository des utilisateurs
│   │   │   │   ├── CredentialRepository.java   # Repository des credentials
│   │   │   │   └── TokenRepository.java        # Repository des tokens
│   │   │   └── service/
│   │   │       └── AuthService.java            # Logique métier d'authentification
│   │   └── resources/
│   │       └── application.properties          # Configuration de l'application
│   └── test/
├── data/                                        # Fichiers de la base de données H2
├── pom.xml                                      # Configuration Maven
└── README.md
```

##  Installation et lancement

### Prérequis

- **Java 17** ou supérieur
- **Maven** 3.6+

### Étapes

1. **Cloner le projet**
   ```bash
   git clone <url-du-repo>
   cd Spring-Auth
   ```

2. **Compiler le projet**
   ```bash
   mvn clean install
   ```

3. **Lancer l'application**
   ```bash
   mvn spring-boot:run
   ```

4. **L'application démarre sur** : `http://localhost:8080`

## API Endpoints

### Authentification de base

#### 1. Créer un compte
```http
POST /auth/create
Content-Type: application/json

{
  "email": "user@example.com",
  "name": "John Doe",
  "password": "motdepasse123"
}
```

**Réponse** (201 Created) :
```json
{
  "message": "User registered successfully",
  "email": "user@example.com",
  "name": "John Doe"
}
```

#### 2. Se connecter
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "motdepasse123"
}
```

**Réponse** (200 OK) :
```json
{
  "message": "Login successful",
  "token": "123e4567-e89b-12d3-a456-426614174000",
  "expirationTime": 1706902800000
}
```

#### 3. Valider un token
```http
GET /auth/validate
Authorization: <token>
```

**Réponse** (200 OK) :
```json
"Token is valid"
```

#### 4. Obtenir les informations de l'utilisateur connecté
```http
GET /auth/me
Authorization: <token>
```

**Réponse** (200 OK) :
```json
{
  "email": "user@example.com",
  "name": "John Doe",
  "token": "123e4567-e89b-12d3-a456-426614174000",
  "expirationTime": 1706902800000,
  "isExpired": false
}
```

#### 5. Se déconnecter
```http
POST /auth/logout
Authorization: <token>
```

**Réponse** (200 OK) :
```json
"Logged out successfully"
```

### Gestion des utilisateurs

#### 6. Lister tous les utilisateurs
```http
GET /auth/users
```

**Réponse** (200 OK) :
```json
{
  "count": 2,
  "users": [
    {
      "id": 1,
      "email": "user1@example.com",
      "name": "John Doe"
    },
    {
      "id": 2,
      "email": "user2@example.com",
      "name": "Jane Smith"
    }
  ]
}
```

#### 7. Lister tous les credentials
```http
GET /auth/credentials
```

**Réponse** (200 OK) :
```json
{
  "count": 2,
  "credentials": [
    {
      "id": 1,
      "email": "user1@example.com",
      "password": "$2a$10$..."
    }
  ]
}
```

#### 8. Supprimer un utilisateur
```http
DELETE /auth/delete/{email}
```

**Réponse** (200 OK) :
```json
{
  "message": "User deleted successfully",
  "email": "user@example.com"
}
```

## Base de données

### Configuration H2

La base de données H2 est configurée pour persister les données dans le dossier `./data/authdb`.

### Console H2

Accédez à la console H2 pour administrer la base de données :

**URL** : `http://localhost:8080/h2-console`

**Paramètres de connexion** :
- **JDBC URL** : `jdbc:h2:file:./data/authdb`
- **Username** : `sa`
- **Password** : *(laisser vide)*

### Tables

Le schéma comprend 3 tables principales :

1. **identities** : Stocke les informations des utilisateurs (email, nom)
2. **credentials** : Stocke les credentials (email, password hashé)
3. **tokens** : Stocke les tokens d'authentification actifs

## Sécurité

- **Hashage des mots de passe** : Utilisation de BCrypt pour le hashage sécurisé
- **Validation des entrées** : Validation côté serveur (email requis, mot de passe minimum 6 caractères)
- **Tokens temporaires** : Les tokens expirent après 1 heure
- **Nettoyage automatique** : Les tokens expirés sont supprimés lors de la validation

## Tests avec Postman

1. Importez la collection Postman (à créer)
2. Créez un compte avec `/auth/create`
3. Connectez-vous avec `/auth/login`
4. Copiez le token reçu
5. Utilisez le token dans le header `Authorization` pour les requêtes protégées

## Notes techniques

- Le port par défaut est **8080** (configurable dans `application.properties`)
- Les logs SQL sont activés pour le débogage
- L'auto-génération du schéma est en mode `update` (conservation des données)

