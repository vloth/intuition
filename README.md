# Intuition

A CLI script to retrieve data from various sources and upsert them into a local database.  
Supported sources:  
* [X] Jenkins builds
* [X] Jira issues 
* [X] Git commits
* [X] Git tags
* [x] Bitbucket pull requests
* [ ] Github issues

### Prerequisites
- [Duckdb v1.0.0 1f98600c2c](https://duckdb.org/)
- [Nodejs v22.6.0](https://nodejs.org/en)

### Installation
```bash
# Clone the repository:
git clone https://github.com/yourusername/intuition.git
cd intuition

# Set up environment variables:
cp .env.sample .env

# Install Node.js dependencies:
npm install

# Start nrepl, run tests or run cli command:
npm run nrepl
npm test
npm run cli -- [--args]
```
