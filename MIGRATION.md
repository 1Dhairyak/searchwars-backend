# Migration Log - Railway to AWS

## Phase 1: Railway (Trial)
- Backend deployed on Railway free trial (1 month).
- Stack: Spring Boot + PostgreSQL.
- Trial expired, service went cold, frontend broke.

## Phase 2: AWS Migration (Free Tier)
- Moving to AWS Elastic Beanstalk + RDS (Postgres).
- Steps: dump DB, provision EB, provision RDS, restore DB, deploy JAR, repoint frontend, kill Railway.

## Status
- [x] Railway deployment (deprecated)
- [ ] AWS EB + RDS setup
- [ ] Data migration
- [ ] Frontend repoint
- [ ] Railway teardown

<!-- progress note 1 -->

<!-- progress note 2 -->

<!-- progress note 3 -->

<!-- progress note 4 -->

<!-- progress note 5 -->

<!-- progress note 6 -->

<!-- progress note 7 -->

<!-- progress note 8 -->

<!-- progress note 9 -->

<!-- progress note 10 -->

<!-- progress note 11 -->

<!-- progress note 12 -->

<!-- progress note 13 -->
