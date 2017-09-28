package com.svb.toiletwall.model.db;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

/**
 * Created by mbodis on 9/25/17.
 */

@Entity(
        active = true
)
public class AnimationFrame {
    @Id
    private Long id;

    @NotNull
    private Long animationId;

    @NotNull
    private int order;

    /**
     * how long (ms) to play this frame
     */
    @NotNull
    private int playMilis;

    /**
     * all rows megrged int one string
     * eg.: 010101011100
     */
    @NotNull
    private String content;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1756513872)
    private transient AnimationFrameDao myDao;


    @Generated(hash = 362009783)
    public AnimationFrame(Long id, @NotNull Long animationId, int order,
            int playMilis, @NotNull String content) {
        this.id = id;
        this.animationId = animationId;
        this.order = order;
        this.playMilis = playMilis;
        this.content = content;
    }

    @Generated(hash = 1261134759)
    public AnimationFrame() {
    }

    
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }


    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getAnimationId() {
        return this.animationId;
    }

    public void setAnimationId(Long animationId) {
        this.animationId = animationId;
    }



    public int getPlayMilis() {
        return this.playMilis;
    }

    public void setPlayMilis(int playMilis) {
        this.playMilis = playMilis;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 436762390)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getAnimationFrameDao() : null;
    }



}
