package com.mojealterego.newgpt.domain.nexus
import java.util.concurrent.atomic.AtomicInteger
data class JobState(val queued:Int,val running:Int,val completed:Int)
class LocalJobEngine{
 private val q=AtomicInteger();private val r=AtomicInteger();private val c=AtomicInteger()
 fun enqueue(){q.incrementAndGet()}
 fun start(){if(q.get()>0){q.decrementAndGet();r.incrementAndGet()}}
 fun complete(){if(r.get()>0){r.decrementAndGet();c.incrementAndGet()}}
 fun state()=JobState(q.get(),r.get(),c.get())
}