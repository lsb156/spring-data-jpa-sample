package com.ssabae.springdatajpa.sample.label

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface LabelRepository : JpaRepository<Label, UUID>